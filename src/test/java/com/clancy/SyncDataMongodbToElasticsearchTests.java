package com.clancy;

import com.clancy.entity.User;
import com.clancy.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SyncDataMongodbToElasticsearchTests {

    @Autowired
    private UserMapper UserMapper;

    @Test
    public void testInsertData() {

        User user1 = User.builder().id(1L).action("test").mac("123").build();
        User user2 = User.builder().id(2L).action("test").mac("234").build();

        UserMapper.saveAll(Arrays.asList(user1, user2)).subscribe(System.out::println);

    }

}
