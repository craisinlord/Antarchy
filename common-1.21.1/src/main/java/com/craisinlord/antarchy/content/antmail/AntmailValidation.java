package com.craisinlord.antarchy.content.antmail;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class AntmailValidation {
    public static final int MAX_SUBJECT_LENGTH = 64;
    public static final int MAX_BODY_LENGTH = 4096;
    public static final int MAX_FILE_NAME_LENGTH = 32;
    public static final int MAX_ATTACHMENTS = 8;
    public static final int MAX_TEXT_ATTACHMENT_CHARACTERS = 16384;
    public static final int MAX_PAINT_WIDTH = 32;
    public static final int MAX_PAINT_HEIGHT = 24;
    public static final int MAX_MESSAGE_BYTES = 65536;
    public static final int MAX_ATTACHMENT_WIRE_CHARS = 65536;
    public static final int MAX_MAILBOX_MESSAGES = 100;
    public static final int MAX_DRAFTS = 25;

    private AntmailValidation() {
    }

    public static Result validateMessage(AntmailAddress sender, AntmailAddress recipient, String subject, String body, List<AntmailAttachment> attachments) {
        if (sender == null || recipient == null) {
            return Result.invalid("missing address");
        }
        if (subject == null || subject.length() > MAX_SUBJECT_LENGTH) {
            return Result.invalid("subject too long");
        }
        if (body == null || body.length() > MAX_BODY_LENGTH) {
            return Result.invalid("body too long");
        }
        Result attachmentResult = validateAttachments(attachments);
        if (!attachmentResult.valid()) return attachmentResult;
        int bytes = subject.getBytes(StandardCharsets.UTF_8).length + body.getBytes(StandardCharsets.UTF_8).length;
        for (AntmailAttachment attachment : attachments) bytes += attachment.byteSize();
        if (bytes > MAX_MESSAGE_BYTES) {
            return Result.invalid("message too large");
        }
        return Result.accepted();
    }

    public static Result validateAttachments(List<AntmailAttachment> attachments) {
        if (attachments == null || attachments.size() > MAX_ATTACHMENTS) return Result.invalid("too many attachments");
        int textCharacters = 0;
        for (AntmailAttachment attachment : attachments) {
            if (attachment == null) return Result.invalid("null attachment");
            if (attachment instanceof AntmailAttachment.TextFile textFile) {
                if (textFile.contents().length() > MAX_TEXT_ATTACHMENT_CHARACTERS) return Result.invalid("text attachment too large");
                textCharacters += textFile.contents().length();
            } else if (attachment instanceof AntmailAttachment.PaintImage paintImage) {
                if (paintImage.width() != MAX_PAINT_WIDTH || paintImage.height() != MAX_PAINT_HEIGHT) return Result.invalid("unsupported paint size");
            }
        }
        if (textCharacters > MAX_TEXT_ATTACHMENT_CHARACTERS) return Result.invalid("text attachments too large");
        return Result.accepted();
    }

    public record Result(boolean valid, String reason) {
        public static Result accepted() {
            return new Result(true, "");
        }

        public static Result invalid(String reason) {
            return new Result(false, reason);
        }
    }
}
