package com.clancy.mapper;

import com.clancy.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author liugang
 * @date 2019/11/1 20:53
 */
public interface UserMapper extends ReactiveMongoRepository<User, Long> {
}
