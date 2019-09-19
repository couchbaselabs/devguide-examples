package com.couchbase.demos.springbootdemo;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

  @Bean
  public Bucket bucket() {
    Cluster cluster = CouchbaseCluster.create("127.0.0.1");
    cluster.authenticate("Administrator", "password");
    return cluster.openBucket("travel-sample");
  }

}


