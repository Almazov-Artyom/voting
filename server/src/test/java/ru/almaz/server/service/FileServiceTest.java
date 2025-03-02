package ru.almaz.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import ru.almaz.server.model.Topic;
import ru.almaz.server.storage.TopicStorage;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class FileServiceTest {
    @Mock
    private TopicStorage topicStorage;

    @InjectMocks
    private FileService fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadFile_FileExists() throws IOException {
        File tempFile = tempDir.resolve("topics.json").toFile();

        ObjectMapper objectMapper = new ObjectMapper();
        List<Topic> testTopics = List.of(new Topic("Topic1"), new Topic("Topic2"));
        objectMapper.writeValue(tempFile, testTopics);

        fileService.loadFile("load " + tempFile.getAbsolutePath());

        verify(topicStorage).setTopics(anyList());
    }

    @Test
    void loadFile_FileNotFound_ShouldPrintError() {
        PrintStream mockPrintStream = Mockito.mock(PrintStream.class);

        System.setOut(mockPrintStream);

        String fakeFilename = tempDir.resolve("non_existent.json").toString();

        fileService.loadFile("load " + fakeFilename);

        verify(mockPrintStream).printf("Файл %s не найден%n", fakeFilename);
    }

    //
    @Test
    void saveFile_ShouldWriteTopicsToFile() throws IOException {
        File tempFile = tempDir.resolve("saved_topics.json").toFile();

        List<Topic> testTopics = List.of(new Topic("Topic1"), new Topic("Topic2"));
        when(topicStorage.findAllTopics()).thenReturn(testTopics);

        // Вызываем метод
        fileService.saveFile("save " + tempFile.getAbsolutePath());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        List<Topic> savedTopics = objectMapper.readValue(tempFile, objectMapper.getTypeFactory().constructCollectionType(List.class, Topic.class));

        assertEquals(testTopics.get(0).getName(), savedTopics.get(0).getName());
        assertEquals(testTopics.get(1).getName(), savedTopics.get(1).getName());
    }

}
