package com.sabareeshrao.intellijlesson.state;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service(Service.Level.PROJECT)
@State(name = "AeroTopoLessonProgress", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public final class LessonProgressService implements PersistentStateComponent<LessonProgressService.ProgressState> {
    private static final int MAX_HISTORY = 250;

    public static final class ProgressState {
        public int lessonNumber = 1;
        public int stepIndex = 0;
        public List<HistoryEntry> history = new ArrayList<>();
    }

    public static final class HistoryEntry {
        public int lessonNumber;
        public int stepIndex;
        public String activeFile = "";
        public int caretOffset;

        public HistoryEntry() {}

        public HistoryEntry(int lessonNumber, int stepIndex, String activeFile, int caretOffset) {
            this.lessonNumber = lessonNumber;
            this.stepIndex = stepIndex;
            this.activeFile = activeFile == null ? "" : activeFile;
            this.caretOffset = Math.max(0, caretOffset);
        }
    }

    private final ProgressState state = new ProgressState();

    @Override
    public ProgressState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull ProgressState loaded) {
        XmlSerializerUtil.copyBean(loaded, state);
        if (state.history == null) {
            state.history = new ArrayList<>();
        }
    }

    public synchronized int lessonNumber() {
        return state.lessonNumber;
    }

    public synchronized int stepIndex() {
        return state.stepIndex;
    }

    public synchronized void setPosition(int lessonNumber, int stepIndex) {
        state.lessonNumber = Math.max(1, lessonNumber);
        state.stepIndex = Math.max(0, stepIndex);
    }

    public synchronized void pushHistory(HistoryEntry entry) {
        state.history.add(entry);
        while (state.history.size() > MAX_HISTORY) {
            state.history.remove(0);
        }
    }

    public synchronized Optional<HistoryEntry> popHistory() {
        if (state.history.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(state.history.remove(state.history.size() - 1));
    }

    public synchronized void clearHistory() {
        state.history.clear();
    }
}
