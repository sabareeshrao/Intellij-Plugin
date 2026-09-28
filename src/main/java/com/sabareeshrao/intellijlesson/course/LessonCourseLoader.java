package com.sabareeshrao.intellijlesson.course;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sabareeshrao.intellijlesson.model.LessonCourse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class LessonCourseLoader {
    public LessonCourse load(Path projectRoot) throws IOException {
        Path normalizedRoot = projectRoot.toAbsolutePath().normalize();
        Path simulationRoot = normalizedRoot.resolve("simulation");
        Path courseFile = simulationRoot.resolve("course.json");

        if (!Files.isRegularFile(courseFile)) {
            throw new IOException("Missing lesson course: " + courseFile);
        }

        JsonObject courseJson = readObject(courseFile);
        List<LessonCourse.BookRef> books = parseBooks(courseJson.getAsJsonArray("books"));
        List<LessonCourse.Chapter> chapters = new ArrayList<>();
        List<LessonCourse.Lesson> lessons = new ArrayList<>();

        JsonArray chapterArray = requiredArray(courseJson, "chapters");
        int ordinal = 0;
        for (JsonElement chapterElement : chapterArray) {
            ordinal++;
            JsonObject chapterJson = chapterElement.getAsJsonObject();
            String chapterId = requiredString(chapterJson, "id");
            String chapterTitle = requiredString(chapterJson, "title");
            String subtitle = optionalString(chapterJson, "subtitle");
            JsonArray lessonPaths = requiredArray(chapterJson, "lessons");
            List<Integer> chapterLessonNumbers = new ArrayList<>();

            for (JsonElement lessonPathElement : lessonPaths) {
                String lessonPath = lessonPathElement.getAsString();
                Path resolved = simulationRoot.resolve(lessonPath).normalize();
                if (!resolved.startsWith(simulationRoot) || !Files.isRegularFile(resolved)) {
                    throw new IOException("Invalid lesson path: " + lessonPath);
                }

                LessonCourse.Lesson lesson = parseLesson(
                        readObject(resolved),
                        chapterId,
                        chapterTitle,
                        ordinal
                );
                chapterLessonNumbers.add(lesson.lessonNumber());
                lessons.add(lesson);
            }

            chapters.add(new LessonCourse.Chapter(
                    chapterId,
                    chapterTitle,
                    subtitle,
                    ordinal,
                    List.copyOf(chapterLessonNumbers)
            ));
        }

        return new LessonCourse(
                requiredString(courseJson, "title"),
                List.copyOf(books),
                List.copyOf(chapters),
                List.copyOf(lessons)
        );
    }

    private static List<LessonCourse.BookRef> parseBooks(JsonArray array) throws IOException {
        if (array == null) {
            return List.of();
        }

        List<LessonCourse.BookRef> books = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject book = element.getAsJsonObject();
            books.add(new LessonCourse.BookRef(
                    requiredString(book, "id"),
                    requiredString(book, "title"),
                    optionalString(book, "subtitle"),
                    requiredInt(book, "chapterStart"),
                    requiredInt(book, "chapterEnd")
            ));
        }
        return books;
    }

    private static LessonCourse.Lesson parseLesson(
            JsonObject lessonJson,
            String chapterId,
            String chapterTitle,
            int chapterOrdinal
    ) throws IOException {
        List<LessonCourse.Step> steps = new ArrayList<>();
        JsonArray stepArray = requiredArray(lessonJson, "steps");

        for (JsonElement stepElement : stepArray) {
            JsonObject step = stepElement.getAsJsonObject();
            JsonObject actionObject = step.has("action") && step.get("action").isJsonObject()
                    ? step.getAsJsonObject("action")
                    : new JsonObject();

            String actionName = optionalString(actionObject, "action");
            JsonObject data = actionObject.has("data") && actionObject.get("data").isJsonObject()
                    ? actionObject.getAsJsonObject("data")
                    : new JsonObject();

            steps.add(new LessonCourse.Step(
                    requiredString(step, "title"),
                    requiredString(step, "question"),
                    step.has("why_te") ? step.get("why_te").getAsString() : optionalString(step, "why"),
                    optionalString(step, "required_capability"),
                    new LessonCourse.Action(actionName, data.deepCopy())
            ));
        }

        if (steps.isEmpty()) {
            throw new IOException("Lesson has no steps: " + requiredInt(lessonJson, "lesson_number"));
        }

        return new LessonCourse.Lesson(
                requiredInt(lessonJson, "lesson_number"),
                requiredInt(lessonJson, "question_id"),
                chapterId,
                chapterTitle,
                chapterOrdinal,
                requiredString(lessonJson, "title"),
                optionalString(lessonJson, "answer"),
                optionalString(lessonJson, "info_language"),
                optionalString(lessonJson, "project_impact"),
                List.copyOf(steps)
        );
    }

    private static JsonObject readObject(Path file) throws IOException {
        String text = Files.readString(file, StandardCharsets.UTF_8);
        JsonElement parsed = JsonParser.parseString(text);
        if (!parsed.isJsonObject()) {
            throw new IOException("Expected JSON object: " + file);
        }
        return parsed.getAsJsonObject();
    }

    private static JsonArray requiredArray(JsonObject object, String name) throws IOException {
        if (!object.has(name) || !object.get(name).isJsonArray()) {
            throw new IOException("Missing JSON array: " + name);
        }
        return object.getAsJsonArray(name);
    }

    private static String requiredString(JsonObject object, String name) throws IOException {
        if (!object.has(name) || object.get(name).isJsonNull()) {
            throw new IOException("Missing JSON string: " + name);
        }
        return object.get(name).getAsString();
    }

    private static String optionalString(JsonObject object, String name) {
        return object.has(name) && !object.get(name).isJsonNull()
                ? object.get(name).getAsString()
                : "";
    }

    private static int requiredInt(JsonObject object, String name) throws IOException {
        if (!object.has(name) || object.get(name).isJsonNull()) {
            throw new IOException("Missing JSON number: " + name);
        }
        return object.get(name).getAsInt();
    }
}
