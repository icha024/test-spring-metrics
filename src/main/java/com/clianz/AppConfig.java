package com.clianz;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import net.sf.log4jdbc.sql.jdbcapi.DataSourceSpy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.MetricsEndpoint;
import org.springframework.boot.actuate.endpoint.MetricsEndpointMetricReader;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.Servlet;
import javax.sql.DataSource;

@Configuration
public class AppConfig {
    @Autowired
    DataSourceProperties dataSourceProperties;

//    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    DataSource realDataSource() {
        DataSource dataSource = DataSourceBuilder
//                .create()
                .create(this.dataSourceProperties.getClassLoader())
                .url(this.dataSourceProperties.getUrl())
                .username(this.dataSourceProperties.getUsername())
                .password(this.dataSourceProperties.getPassword())
                .build();
        return dataSource;
    }

    @Bean
    @Primary
    DataSource dataSource() {
        return new DataSourceSpy(realDataSource());
    }

//    /*
//     * Reading all metrics that appear on the /metrics endpoint to expose them to metrics writer beans.
//     */
//    @Bean
//    public MetricsEndpointMetricReader metricsEndpointMetricReader(final MetricsEndpoint metricsEndpoint) {
//        return new MetricsEndpointMetricReader(metricsEndpoint);
//    }

//    @Bean
//    public DispatcherServlet dispatcherServlet() {
//        DispatcherServlet servlet=new DispatcherServlet();
//        servlet.getServletContext().addListener(new ExecutorListener());
//        return  servlet;
//    }

    @Bean
    public ServletRegistrationBean dropWizardHttp(MetricRegistry metricRegistry) {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(
                new MetricsServlet(metricRegistry), "/dw");
        servletRegistrationBean.setLoadOnStartup(1);
        return servletRegistrationBean;
    }

//    @Bean
//    public MetricRegistry metricRegistry() {
//        return new MetricRegistry();
//    }
}