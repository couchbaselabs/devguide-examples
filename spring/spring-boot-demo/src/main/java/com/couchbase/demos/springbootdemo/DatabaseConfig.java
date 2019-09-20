package com.couchbase.demos.springbootdemo;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

  @Value("${db.host}")
  private String host;

  @Value("${db.user}")
  private String user;

  @Value("${db.password}")
  private String password;

  @Value("${db.bucket}")
  private String bucket;

  @Bean
  public Bucket bucket() {
    Cluster cluster = CouchbaseCluster.create(host);
    cluster.authenticate(user, password);
    return cluster.openBucket(bucket);
  }

}


