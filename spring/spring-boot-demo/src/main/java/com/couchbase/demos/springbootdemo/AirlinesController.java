package com.couchbase.demos.springbootdemo;

import com.couchbase.client.java.Bucket;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rx.RxReactiveStreams;

import java.util.Map;

@RestController
@RequestMapping("/airlines")
public class AirlinesController {

  private final Bucket bucket;

  @Autowired
  public AirlinesController(Bucket bucket) {
    this.bucket = bucket;
  }

  @GetMapping(value = "/{id}")
  public Publisher<ResponseEntity<Map<String, Object>>> findById(final @PathVariable("id") String id) {
    return RxReactiveStreams.toPublisher(
      bucket.async()
        .get(id)
        .map(doc -> ResponseEntity.ok(doc.content().toMap()))
        .singleOrDefault(ResponseEntity.notFound().build())
    );
  }

}
