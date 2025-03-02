package ru.almaz.server.service;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.almaz.server.handler.AnswerHandler;
import ru.almaz.server.handler.VoteHandler;
import ru.almaz.server.manager.VoteManager;
import ru.almaz.server.model.Topic;
import ru.almaz.server.model.Vote;
import ru.almaz.server.storage.TopicStorage;
import ru.almaz.server.storage.UserStorage;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;


public class VoteServiceTest {
    @Mock
    private ChannelHandlerContext ctx;

    @Mock
    private Channel channel;

    @Mock
    private ChannelPipeline pipeline;

    @Mock
    private TopicStorage topicStorage;

    @Mock
    private UserStorage userStorage;

    @Mock
    private VoteManager voteManager;

    @InjectMocks
    private VoteService voteService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(ctx.pipeline()).thenReturn(pipeline);
        when(ctx.channel()).thenReturn(channel);
    }

    @Test
    public void startCreateVote_TopicExists() {
        String topicName = "TestTopic";
        String msg = "create vote -t=" + topicName;
        Topic topic = new Topic("TestTopic");

        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.of(topic));

        voteService.startCreateVote(ctx, msg);

        verify(ctx).writeAndFlush("Введите название голосования\n");

        verify(ctx.pipeline()).removeLast();
        verify(ctx.pipeline()).addLast(any(VoteHandler.class));

    }

    @Test
    public void startCreateVote_TopicNotExists() {
        String topicName = "nonExistentTopic";
        String msg = "create vote -t=" + topicName;
        when(topicStorage.isTopicExists(topicName)).thenReturn(false);

        voteService.startCreateVote(ctx, msg);

        verify(ctx).writeAndFlush("Такого топика не существует\n");

        verify(ctx.pipeline(), never()).removeLast();
        verify(ctx.pipeline(), never()).addLast(any(VoteHandler.class));
    }

    @Test
    public void view_TopicNotExists() {
        String topicName = "TopicNotExists";
        String voteName = "TestVote";
        String msg = "create vote -t=" + topicName + " -v=" + voteName;

        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.empty());

        voteService.view(ctx, msg);

        verify(ctx).writeAndFlush("Такого топика не существует\n");
    }

    @Test
    public void view_TopicExistsVoteNotExists() {
        String topicName = "TestTopic";
        String voteName = "VoteNotExists";
        String msg = "create vote -t=" + topicName + " -v=" + voteName;

        Topic topic = new Topic(topicName);

        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.of(new Topic(topicName)));
        when(voteManager.findVoteByTopic(topic, voteName)).thenReturn(Optional.empty());

        voteService.view(ctx, msg);

        verify(ctx).writeAndFlush("Такого голосования не существует\n");
    }

    @Test
    public void view_TopicExistsVoteExists() {
        String topicName = "TestTopic";
        String voteName = "TestVote";
        String msg = "create vote -t=" + topicName + " -v=" + voteName;

        Topic topic = new Topic(topicName);

        Vote vote = new Vote();
        vote.setName("TestVote");
        vote.setDescription("TestDescription");

        Vote.AnswerOption answerOption1 = new Vote.AnswerOption("TestAnswerOption1");
        answerOption1.setCountUsers(5);
        Vote.AnswerOption answerOption2 = new Vote.AnswerOption("TestAnswerOption2");
        answerOption2.setCountUsers(6);

        vote.getAnswerOptions().add(answerOption1);
        vote.getAnswerOptions().add(answerOption2);


        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.of(topic));
        when(voteManager.findVoteByTopic(topic, voteName)).thenReturn(Optional.of(vote));

        voteService.view(ctx, msg);

        verify(ctx).writeAndFlush("TestDescription\n");
        verify(ctx).writeAndFlush("TestAnswerOption1 (5 пользователей выбрали этот вариант)\n");
        verify(ctx).writeAndFlush("TestAnswerOption2 (6 пользователей выбрали этот вариант)\n");
    }

    @Test
    public void startVote_TopicNoExists() {
        String topicName = "TopicNoExists";
        String voteName = "TestVote";
        String msg = "vote -t=" + topicName + " -v=" + voteName;

        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.empty());

        voteService.startVote(ctx, msg);

        verify(ctx).writeAndFlush("Такого топика не существует\n");
    }

    @Test
    public void startVote_TopicExistsVoteNotExists() {
        String topicName = "TestTopic";
        String voteName = "VoteNotExists";
        String msg = "vote -t=" + topicName + " -v=" + voteName;
        Topic topic = new Topic(topicName);

        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.of(topic));
        when(voteManager.findVoteByTopic(topic, voteName)).thenReturn(Optional.empty());

        voteService.startVote(ctx, msg);

        verify(ctx).writeAndFlush("Такого голосования не существует\n");
    }

    @Test
    void startVote_TopicExistsVoteExistsUserVoted() {
        String topicName = "TestTopic";
        String voteName = "TestVote";
        String msg = "vote -t=" + topicName + " -v=" + voteName;
        String user = "TestUser";

        Topic topic = new Topic(topicName);
        Vote vote = new Vote();

        vote.getAnswerUsers().add(user);
        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.of(topic));
        when(voteManager.findVoteByTopic(topic, voteName)).thenReturn(Optional.of(vote));
        when(userStorage.findUserByChannel(ctx.channel())).thenReturn(user);

        voteService.startVote(ctx, msg);

        verify(ctx).writeAndFlush("Вы уже отвечали\n");
    }

    @Test
    void startVote_TopicExistsVoteNotExistsUserNotVoted() {
        String topicName = "TestTopic";
        String voteName = "TestVote";
        String msg = "vote -t=" + topicName + " -v=" + voteName;
        String user = "TestUser";

        Topic topic = new Topic(topicName);

        Vote vote = new Vote();
        vote.setName("TestVote");
        vote.setDescription("TestDescription");

        Vote.AnswerOption answerOption1 = new Vote.AnswerOption("TestAnswerOption1");
        answerOption1.setCountUsers(5);
        Vote.AnswerOption answerOption2 = new Vote.AnswerOption("TestAnswerOption2");
        answerOption2.setCountUsers(6);

        vote.getAnswerOptions().add(answerOption1);
        vote.getAnswerOptions().add(answerOption2);

        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.of(topic));
        when(voteManager.findVoteByTopic(topic, voteName)).thenReturn(Optional.of(vote));
        when(userStorage.findUserByChannel(ctx.channel())).thenReturn(user);

        voteService.startVote(ctx, msg);

        verify(ctx).writeAndFlush("1. TestAnswerOption1\n");
        verify(ctx).writeAndFlush("2. TestAnswerOption2\n");
        verify(ctx).writeAndFlush(ctx.writeAndFlush("Выберите ответ\n"));
        verify(ctx.pipeline()).removeLast();
        verify(ctx.pipeline()).addLast(any(AnswerHandler.class));
    }

    @Test
    public void deleteVote_TopicNoExists() {
        String topicName = "TopicNoExists";
        String voteName = "TestVote";
        String msg = "vote -t=" + topicName + " -v=" + voteName;

        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.empty());

        voteService.deleteVote(ctx, msg);

        verify(ctx).writeAndFlush("Такого топика не существует\n");
    }

    @Test
    public void deleteVote_TopicExistsVoteNotExists() {
        String topicName = "TestTopic";
        String voteName = "VoteNotExists";
        String msg = "vote -t=" + topicName + " -v=" + voteName;
        Topic topic = new Topic(topicName);

        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.of(topic));
        when(voteManager.findVoteByTopic(topic, voteName)).thenReturn(Optional.empty());

        voteService.deleteVote(ctx, msg);

        verify(ctx).writeAndFlush("Такого голосования не существует\n");
    }

    @Test
    public void deleteVote_TopicExistsVoteExistsUserNotCreator() {
        String topicName = "TestTopic";
        String voteName = "TestVote";
        String msg = "vote -t=" + topicName + " -v=" + voteName;
        String user = "TestUser";

        Topic topic = new Topic(topicName);

        Vote vote = new Vote();
        vote.setUserCreator(user);

        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.of(topic));
        when(voteManager.findVoteByTopic(topic, voteName)).thenReturn(Optional.of(vote));
        when(userStorage.findUserByChannel(ctx.channel())).thenReturn("TestUser1");

        voteService.deleteVote(ctx, msg);

        verify(ctx).writeAndFlush("Голосование может удалить только пользователь, создавший его\n");
    }

    @Test
    public void deleteVote_TopicExistsVoteNotExistsUserIsCreator() {
        String topicName = "TestTopic";
        String voteName = "TestVote";
        String msg = "vote -t=" + topicName + " -v=" + voteName;
        String user = "TestUser";

        Topic topic = new Topic(topicName);

        Vote vote = new Vote();
        vote.setUserCreator(user);

        topic.getVotes().add(vote);

        when(topicStorage.findTopicByName(topicName)).thenReturn(Optional.of(topic));
        when(voteManager.findVoteByTopic(topic, voteName)).thenReturn(Optional.of(vote));
        when(userStorage.findUserByChannel(ctx.channel())).thenReturn(user);

        voteService.deleteVote(ctx, msg);

        Assertions.assertEquals(0, topic.getVotes().size());
    }
}
