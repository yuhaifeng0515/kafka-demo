package com.example.kafkademo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;

@Configuration
public class KafkaInitialConfiguration {
    @Autowired
    private ConsumerFactory consumerFactory;

    //创建一个名为testtopic的Topic并设置分区数为8，分区副本数为2
    @Bean
    public NewTopic initialTopic(){
        return new NewTopic("testtopic", 8, (short)1);
    }

    //如果要修改分区数，只需修改配置值重启项目即可
    //修改分区数并不会导致数据的丢失，但是分区数只能增大不能减少
    @Bean
    public NewTopic updateTopic(){
        return new NewTopic("testtopic", 10, (short)1);
    }

    @Bean
    public ConsumerAwareListenerErrorHandler consumerAwareErrorHandler(){
        return (message, exception, consumer) -> {
            System.out.println("消费异常：" + message.getPayload());
            return null;
        };
    }

    //消息过滤器
    @Bean
    public ConcurrentKafkaListenerContainerFactory filterContainerFactory(){
        ConcurrentKafkaListenerContainerFactory factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactory);
        //被过滤的消息将被丢弃
        factory.setAckDiscarded(true);
        //消息过滤策略
        factory.setRecordFilterStrategy(consumerRecord -> {
            if(Integer.parseInt(consumerRecord.value().toString()) % 2 == 0){
                return false;
            }
            //返回true消息则被过滤
            return true;
        });
        return factory;
    }

    //监听器容器工厂（设置禁止KafkaListener自启动）
    @Bean
    public ConcurrentKafkaListenerContainerFactory delayContainerFactory() {
        ConcurrentKafkaListenerContainerFactory container = new ConcurrentKafkaListenerContainerFactory();
        container.setConsumerFactory(consumerFactory);
        //禁止kafkaListener自启动
        container.setAutoStartup(false);
        return container;
    }
}
