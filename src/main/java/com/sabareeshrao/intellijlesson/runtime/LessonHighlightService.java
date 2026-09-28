package com.sabareeshrao.intellijlesson.runtime;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;

import java.awt.*;

@Service(Service.Level.PROJECT)
public final class LessonHighlightService {
    private RangeHighlighter active;

    public void clear() {
        if (active != null && active.isValid()) {
            active.dispose();
        }
        active = null;
    }

    public void highlightLine(Editor editor, int zeroBasedLine) {
        clear();

        int safeLine = Math.max(0, Math.min(zeroBasedLine, editor.getDocument().getLineCount() - 1));
        int start = editor.getDocument().getLineStartOffset(safeLine);
        int end = editor.getDocument().getLineEndOffset(safeLine);

        TextAttributes attributes = new TextAttributes();
        attributes.setBackgroundColor(new JBColor(
                new Color(255, 235, 59, 95),
                new Color(255, 190, 0, 80)
        ));

        active = editor.getMarkupModel().addRangeHighlighter(
                start,
                Math.max(start, end),
                HighlighterLayer.SELECTION - 1,
                attributes,
                HighlighterTargetArea.LINES_IN_RANGE
        );

        editor.getCaretModel().moveToOffset(start);
        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
    }
}
