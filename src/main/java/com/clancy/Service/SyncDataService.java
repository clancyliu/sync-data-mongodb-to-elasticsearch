package com.clancy.Service;

import com.alibaba.fastjson.JSONObject;
import com.clancy.entity.User;
import com.clancy.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author liugang
 * @date 2019/11/1 20:52
 */
@Slf4j
@Component
public class SyncDataService implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RestHighLevelClient highLevelClient;

    @Autowired
    private ReactiveMongoOperations reactiveTemplate;

    @Override
    public void run(String... args) throws Exception {

        // 以流的形式，同步老的离线数据
        userMapper.findAll().subscribe(this::saveData);


        // 以Change Stream 的方式同步新的实时数据
        Flux<ChangeStreamEvent<User>> changeStreamEventFlux = reactiveTemplate.changeStream("user",
                // 新增的数据才会通知
                ChangeStreamOptions.builder().filter(newAggregation(match(where("operationType").is("insert")))).build(), User.class);
        changeStreamEventFlux.subscribe(changeStreamEvent -> this.saveData(Objects.requireNonNull(changeStreamEvent.getBody())));
    }

    private void saveData(User user) {
        IndexRequest indexRequest = new IndexRequest("user")
                .id(user.getId().toString())
                .source(JSONObject.toJSONString(user), XContentType.JSON);
        try {
            highLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("sync data error: {}", user);
        }
    }

    private boolean indexExist(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return highLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    private boolean createIndex(String indexName, String jsonMapping) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        request.mapping(jsonMapping, XContentType.JSON);
        CreateIndexResponse response = highLevelClient.indices().create(request, RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }

    private String getMapping() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("user.json");
        return IOUtils.toString(classPathResource.getInputStream(), Charset.defaultCharset());
    }

}
