package ru.almaz.server.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.almaz.server.model.Topic;
import ru.almaz.server.model.Vote;
import ru.almaz.server.storage.TopicStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TopicServiceTest {
    @Mock
    private ChannelHandlerContext ctx;

    @Mock
    private Channel channel;

    @Mock
    private TopicStorage topicStorage;

    @InjectMocks
    private TopicService topicService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(ctx.channel()).thenReturn(channel);
    }

    @Test
    public void createTopic_TopicNotExist() {
        String topicName = "TestTopic";
        String msg = "create topic -n=" + topicName;

        when(topicStorage.isTopicExists(topicName)).thenReturn(false);

        topicService.createTopic(ctx, msg);

        verify(topicStorage).saveTopic(any(Topic.class));
        verify(ctx).writeAndFlush("Вы создали топик с именем: TestTopic\n");
    }

    @Test
    public void createTopic_TopicExist() {
        String topicName = "TestTopic";
        String msg = "create topic -n=" + topicName;

        when(topicStorage.isTopicExists(topicName)).thenReturn(true);

        topicService.createTopic(ctx, msg);

        verify(ctx).writeAndFlush("Топик с таким именем уже существует\n");
    }

    @Test
    public void view_TopicsNotExist() {
        String msg = "view";

        when(topicStorage.findAllTopics()).thenReturn(new ArrayList<>());

        topicService.view(ctx, msg);
        verify(ctx).writeAndFlush("Топиков нет\n");
    }

    @Test
    public void view_TopicsExist() {
        String msg = "view";

        List<Topic> topics = new ArrayList<>();
        Topic topic = new Topic("TestTopic");
        topic.getVotes().add(new Vote());
        topics.add(topic);

        when(topicStorage.findAllTopics()).thenReturn(topics);

        topicService.view(ctx, msg);

        verify(ctx).writeAndFlush("TestTopic (votes in topic = 1)\n");
    }

    @Test
    public void viewPrefixT_TopicNotExist() {
        String topicName = "TestTopic";
        String msg = "view -t=" + topicName;

        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.empty());

        topicService.viewPrefixT(ctx, msg);

        verify(ctx).writeAndFlush("Такого топика не существует\n");

    }

    @Test
    public void viewPrefixT_TopicExist() {
        String topicName = "TestTopic";
        String msg = "view -t=" + topicName;

        Topic topic = new Topic("TestTopic");
        Vote vote = new Vote();
        vote.setName("TestVote");
        topic.getVotes().add(vote);

        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.of(topic));

        topicService.viewPrefixT(ctx, msg);

        verify(ctx).writeAndFlush("TestVote\n");
    }


}
