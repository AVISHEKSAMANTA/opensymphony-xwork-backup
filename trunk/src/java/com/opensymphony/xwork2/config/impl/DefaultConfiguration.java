/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.xwork2.config.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ognl.PropertyAccessor;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.DefaultTextProvider;
import com.opensymphony.xwork2.ObjectFactory;
import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationException;
import com.opensymphony.xwork2.config.ConfigurationProvider;
import com.opensymphony.xwork2.config.ContainerProvider;
import com.opensymphony.xwork2.config.PackageProvider;
import com.opensymphony.xwork2.config.RuntimeConfiguration;
import com.opensymphony.xwork2.config.entities.ActionConfig;
import com.opensymphony.xwork2.config.entities.ExceptionMappingConfig;
import com.opensymphony.xwork2.config.entities.InterceptorMapping;
import com.opensymphony.xwork2.config.entities.PackageConfig;
import com.opensymphony.xwork2.config.entities.ResultConfig;
import com.opensymphony.xwork2.config.entities.ResultTypeConfig;
import com.opensymphony.xwork2.config.providers.InterceptorBuilder;
import com.opensymphony.xwork2.conversion.ObjectTypeDeterminer;
import com.opensymphony.xwork2.conversion.impl.DefaultObjectTypeDeterminer;
import com.opensymphony.xwork2.conversion.impl.XWorkBasicConverter;
import com.opensymphony.xwork2.conversion.impl.XWorkConverter;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.ContainerBuilder;
import com.opensymphony.xwork2.inject.Context;
import com.opensymphony.xwork2.inject.Factory;
import com.opensymphony.xwork2.inject.Scope;
import com.opensymphony.xwork2.ognl.OgnlReflectionProvider;
import com.opensymphony.xwork2.ognl.OgnlUtil;
import com.opensymphony.xwork2.ognl.OgnlValueStackFactory;
import com.opensymphony.xwork2.ognl.accessor.CompoundRootAccessor;
import com.opensymphony.xwork2.util.CompoundRoot;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import com.opensymphony.xwork2.util.location.LocatableProperties;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import com.opensymphony.xwork2.util.reflection.ReflectionProvider;


/**
 * DefaultConfiguration
 *
 * @author Jason Carreira
 *         Created Feb 24, 2003 7:38:06 AM
 */
public class DefaultConfiguration implements Configuration {

    protected static final Logger LOG = LoggerFactory.getLogger(DefaultConfiguration.class);


    // Programmatic Action Configurations
    protected Map<String, PackageConfig> packageContexts = new LinkedHashMap<String, PackageConfig>();
    protected RuntimeConfiguration runtimeConfiguration;
    protected Container container;
    protected String defaultFrameworkBeanName;
    protected Set<String> loadedFileNames = new TreeSet<String>();


    ObjectFactory objectFactory;

    public DefaultConfiguration() {
        this("xwork");
    }
    
    public DefaultConfiguration(String defaultBeanName) {
        this.defaultFrameworkBeanName = defaultBeanName;
    }


    public PackageConfig getPackageConfig(String name) {
        return packageContexts.get(name);
    }

    public Set getPackageConfigNames() {
        return packageContexts.keySet();
    }

    public Map getPackageConfigs() {
        return packageContexts;
    }
    
    public Set<String> getLoadedFileNames() {
        return loadedFileNames;
    }

    public RuntimeConfiguration getRuntimeConfiguration() {
        return runtimeConfiguration;
    }
    
    /**
     * @return the container
     */
    public Container getContainer() {
        return container;
    }

    public void addPackageConfig(String name, PackageConfig packageContext) {
        PackageConfig check = packageContexts.get(name);
        if (check != null) {
            if (check.getLocation() != null && packageContext.getLocation() != null
                    && check.getLocation().equals(packageContext.getLocation())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("The package name '" + name 
                    + "' is already been loaded by the same location and could be removed: " 
                    + packageContext.getLocation());
                } 
            } else {
                throw new ConfigurationException("The package name '" + name 
                        + "' at location "+packageContext.getLocation()
                        + " is already been used by another package at location " + check.getLocation(),
                        packageContext);
            }
        }
        packageContexts.put(name, packageContext);
    }

    /**
     * Allows the configuration to clean up any resources used
     */
    public void destroy() {
        packageContexts.clear();
        loadedFileNames.clear();
    }

    public void rebuildRuntimeConfiguration() {
        runtimeConfiguration = buildRuntimeConfiguration();
    }
    
    /**
     * Calls the ConfigurationProviderFactory.getConfig() to tell it to reload the configuration and then calls
     * buildRuntimeConfiguration().
     *
     * @throws ConfigurationException
     */
    public synchronized void reload(List<ConfigurationProvider> providers) throws ConfigurationException {
        
        // Silly copy necessary due to lack of ability to cast generic lists
        List<ContainerProvider> contProviders = new ArrayList<ContainerProvider>();
        contProviders.addAll(providers);
        
        reloadContainer(contProviders);
    }

    /**
     * Calls the ConfigurationProviderFactory.getConfig() to tell it to reload the configuration and then calls
     * buildRuntimeConfiguration().
     *
     * @throws ConfigurationException
     */
    public synchronized List<PackageProvider> reloadContainer(List<ContainerProvider> providers) throws ConfigurationException {
        packageContexts.clear();
        loadedFileNames.clear();
        List<PackageProvider> packageProviders = new ArrayList<PackageProvider>();

        ContainerProperties props = new ContainerProperties();
        ContainerBuilder builder = new ContainerBuilder();
        for (final ContainerProvider containerProvider : providers)
        {
            containerProvider.init(this);
            containerProvider.register(builder, props);
        }
        props.setConstants(builder);
        
        builder.factory(Configuration.class, new Factory<Configuration>() {
            public Configuration create(Context context) throws Exception {
                return DefaultConfiguration.this;
            }
        });
        
        try {
            // Set the bootstrap container for the purposes of factory creation
            Container bootstrap = createBootstrapContainer();
            setContext(bootstrap);
            container = builder.create(false);
            setContext(container);
            objectFactory = container.getInstance(ObjectFactory.class);

            // Process the configuration providers first
            for (final ContainerProvider containerProvider : providers)
            {
                if (containerProvider instanceof PackageProvider) {
                    container.inject(containerProvider);
                    ((PackageProvider)containerProvider).loadPackages();
                    packageProviders.add((PackageProvider)containerProvider);
                }
            }
            
            // Then process any package providers from the plugins
            Set<String> packageProviderNames = container.getInstanceNames(PackageProvider.class);
            if (packageProviderNames != null) {
                for (String name : packageProviderNames) {
                    PackageProvider provider = container.getInstance(PackageProvider.class, name);
                    provider.init(this);
                    provider.loadPackages();
                    packageProviders.add(provider);
                }
            }
    
            rebuildRuntimeConfiguration();
        } finally {
            ActionContext.setContext(null);
        }
        return packageProviders;
    }
    
    protected ActionContext setContext(Container cont) {
        ValueStack vs = cont.getInstance(ValueStackFactory.class).createValueStack();
        ActionContext context = new ActionContext(vs.getContext());
        ActionContext.setContext(context);
        return context;
    }

    protected Container createBootstrapContainer() {
        ContainerBuilder builder = new ContainerBuilder();
        builder.factory(ObjectFactory.class, Scope.SINGLETON);
        builder.factory(ReflectionProvider.class, OgnlReflectionProvider.class, Scope.SINGLETON);
        builder.factory(ValueStackFactory.class, OgnlValueStackFactory.class, Scope.SINGLETON);
        builder.factory(XWorkConverter.class, Scope.SINGLETON);
        builder.factory(XWorkBasicConverter.class, Scope.SINGLETON);
        builder.factory(TextProvider.class, "system", DefaultTextProvider.class, Scope.SINGLETON);
        builder.factory(ObjectTypeDeterminer.class, DefaultObjectTypeDeterminer.class, Scope.SINGLETON);
        builder.factory(PropertyAccessor.class, CompoundRoot.class.getName(), CompoundRootAccessor.class, Scope.SINGLETON);
        builder.factory(OgnlUtil.class, Scope.SINGLETON);
        builder.constant("devMode", "false");
        return builder.create(true);
    }

    public void removePackageConfig(String name) {
        PackageConfig toBeRemoved = packageContexts.get(name);

        if (toBeRemoved != null) {
            for (PackageConfig packageConfig : packageContexts.values()) {
                packageConfig.removeParent(toBeRemoved);
            }
        }
    }

    /**
     * This methodName builds the internal runtime configuration used by Xwork for finding and configuring Actions from the
     * programmatic configuration data structures. All of the old runtime configuration will be discarded and rebuilt.
     */
    protected synchronized RuntimeConfiguration buildRuntimeConfiguration() throws ConfigurationException {
        Map<String, Map<String, ActionConfig>> namespaceActionConfigs = new LinkedHashMap<String, Map<String, ActionConfig>>();
        Map<String, String> namespaceConfigs = new LinkedHashMap<String, String>();

        for (PackageConfig packageConfig : packageContexts.values()) {

            if (!packageConfig.isAbstract()) {
                String namespace = packageConfig.getNamespace();
                Map<String, ActionConfig> configs = namespaceActionConfigs.get(namespace);

                if (configs == null) {
                    configs = new LinkedHashMap<String, ActionConfig>();
                }

                Map actionConfigs = packageConfig.getAllActionConfigs();

                for (Object o : actionConfigs.keySet()) {
                    String actionName = (String) o;
                    ActionConfig baseConfig = (ActionConfig) actionConfigs.get(actionName);
                    configs.put(actionName, buildFullActionConfig(packageConfig, baseConfig));
                }

                namespaceActionConfigs.put(namespace, configs);
                if (packageConfig.getFullDefaultActionRef() != null) {
                    namespaceConfigs.put(namespace, packageConfig.getFullDefaultActionRef());
                }
            }
        }

        return new RuntimeConfigurationImpl(namespaceActionConfigs, namespaceConfigs);
    }

    private void setDefaultResults(Map<String, ResultConfig> results, PackageConfig packageContext) {
        String defaultResult = packageContext.getFullDefaultResultType();

        for (Map.Entry<String, ResultConfig> entry : results.entrySet()) {

            if (entry.getValue() == null) {
                ResultTypeConfig resultTypeConfig = packageContext.getAllResultTypeConfigs().get(defaultResult);
                entry.setValue(new ResultConfig(null, resultTypeConfig.getClazz()));
            }
        }
    }

    /**
     * Builds the full runtime actionconfig with all of the defaults and inheritance
     *
     * @param packageContext the PackageConfig which holds the base config we're building from
     * @param baseConfig     the ActionConfig which holds only the configuration specific to itself, without the defaults
     *                       and inheritance
     * @return a full ActionConfig for runtime configuration with all of the inherited and default params
     * @throws com.opensymphony.xwork2.config.ConfigurationException
     *
     */
    private ActionConfig buildFullActionConfig(PackageConfig packageContext, ActionConfig baseConfig) throws ConfigurationException {
        Map<String, Object> params = new TreeMap<String, Object>(baseConfig.getParams());
        Map<String, ResultConfig> results = new TreeMap<String, ResultConfig>();

        if (!baseConfig.getPackageName().equals(packageContext.getName()) && packageContexts.containsKey(baseConfig.getPackageName())) {
            results.putAll(packageContexts.get(baseConfig.getPackageName()).getAllGlobalResults());
        } else {
            results.putAll(packageContext.getAllGlobalResults());
        }

       	results.putAll(baseConfig.getResults());

        setDefaultResults(results, packageContext);

        List<InterceptorMapping> interceptors = new ArrayList<InterceptorMapping>(baseConfig.getInterceptors());

        if (interceptors.size() <= 0) {
            String defaultInterceptorRefName = packageContext.getFullDefaultInterceptorRef();

            if (defaultInterceptorRefName != null) {
                interceptors.addAll(InterceptorBuilder.constructInterceptorReference(packageContext, defaultInterceptorRefName, 
                        new LinkedHashMap(), packageContext.getLocation(), objectFactory));
            }
        }

        List<ExceptionMappingConfig> exceptionMappings = baseConfig.getExceptionMappings();
        exceptionMappings.addAll(packageContext.getAllExceptionMappingConfigs());

        ActionConfig config = new ActionConfig(baseConfig.getMethodName(), baseConfig.getClassName(), packageContext.getName(), params, results,
                interceptors, exceptionMappings);
        config.setLocation(baseConfig.getLocation());
        return config;
    }


    private class RuntimeConfigurationImpl implements RuntimeConfiguration {
        private Map<String, Map<String, ActionConfig>> namespaceActionConfigs;
        private Map<String, ActionConfigMatcher> namespaceActionConfigMatchers;
        private Map<String, String> namespaceConfigs;

        public RuntimeConfigurationImpl(Map<String, Map<String, ActionConfig>> namespaceActionConfigs, Map<String, String> namespaceConfigs) {
            this.namespaceActionConfigs = namespaceActionConfigs;
            this.namespaceConfigs = namespaceConfigs;
            
            this.namespaceActionConfigMatchers = new LinkedHashMap<String, ActionConfigMatcher>();
            
            for (String ns : namespaceActionConfigs.keySet()) {
                namespaceActionConfigMatchers.put(ns,
                        new ActionConfigMatcher(namespaceActionConfigs.get(ns), true));
            }
        }


        /**
         * Gets the configuration information for an action name, or returns null if the
         * name is not recognized.
         *
         * @param name      the name of the action
         * @param namespace the namespace for the action or null for the empty namespace, ""
         * @return the configuration information for action requested
         */
        public synchronized ActionConfig getActionConfig(String namespace, String name) {
            ActionConfig config = null;
            Map<String, ActionConfig> actions = namespaceActionConfigs.get((namespace == null) ? "" : namespace);
            if (actions != null) {
                config = actions.get(name);
                // Check wildcards
                if (config == null) {
                    config = namespaceActionConfigMatchers.get(namespace).match(name);
                    // fail over to default action
                    if (config == null) {
                        String defaultActionRef = namespaceConfigs.get((namespace == null) ? "" : namespace);
                        if (defaultActionRef != null) {
                            config = actions.get(defaultActionRef);
                        }
                    }
                }
            }

            // fail over to empty namespace
            if ((config == null) && (namespace != null) && (!namespace.trim().equals(""))) {
                actions = namespaceActionConfigs.get("");

                if (actions != null) {
                    config = actions.get(name);
                    // Check wildcards
                    if (config == null) {
                        config = namespaceActionConfigMatchers.get("").match(name);
                        // fail over to default action
                        if (config == null) {
                            String defaultActionRef = namespaceConfigs.get("");
                            if (defaultActionRef != null) {
                                config = actions.get(defaultActionRef);
                            }
                        }
                    }
                }
            }


            return config;
        }
        
        /**
         * Gets the configuration settings for every action.
         *
         * @return a Map of namespace - > Map of ActionConfig objects, with the key being the action name
         */
        public synchronized Map getActionConfigs() {
            return namespaceActionConfigs;
        }

        public String toString() {
            StringBuffer buff = new StringBuffer("RuntimeConfiguration - actions are\n");

            for (String namespace : namespaceActionConfigs.keySet()) {
                Map<String, ActionConfig> actionConfigs = namespaceActionConfigs.get(namespace);

                for (String s : actionConfigs.keySet()) {
                    buff.append(namespace).append("/").append(s).append("\n");
                }
            }

            return buff.toString();
        }
    }
    
    class ContainerProperties extends LocatableProperties {
        private static final long serialVersionUID = -7320625750836896089L;

        public Object setProperty(String key, String value) {
            String oldValue = getProperty(key);
            if (oldValue != null && !oldValue.equals(value) && !defaultFrameworkBeanName.equals(oldValue)) {
                LOG.info("Overriding property "+key+" - old value: "+oldValue+" new value: "+value);
            }
            return super.setProperty(key, value);
        }

        public void setConstants(ContainerBuilder builder) {
            for (Object keyobj : keySet()) {
                String key = (String)keyobj;
                builder.factory(String.class, key, 
                        new LocatableConstantFactory<String>(getProperty(key), getPropertyLocation(key)));
            }
        }
    }
}
