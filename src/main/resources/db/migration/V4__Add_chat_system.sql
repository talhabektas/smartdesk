-- Chat Messages Table
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT',
    is_internal BOOLEAN NOT NULL DEFAULT FALSE,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP NULL,
    reply_to_message_id BIGINT NULL,
    file_url VARCHAR(500) NULL,
    file_name VARCHAR(255) NULL,
    file_size BIGINT NULL,
    file_type VARCHAR(100) NULL,
    ticket_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    
    -- Foreign Keys
    CONSTRAINT fk_chat_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_reply FOREIGN KEY (reply_to_message_id) REFERENCES chat_messages(id) ON DELETE SET NULL,
    CONSTRAINT fk_chat_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_chat_updated_by FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for performance
CREATE INDEX idx_chat_ticket ON chat_messages(ticket_id);
CREATE INDEX idx_chat_sender ON chat_messages(sender_id);
CREATE INDEX idx_chat_created_at ON chat_messages(created_at);
CREATE INDEX idx_chat_message_type ON chat_messages(message_type);
CREATE INDEX idx_chat_is_read ON chat_messages(is_read);
CREATE INDEX idx_chat_reply_to ON chat_messages(reply_to_message_id);
CREATE INDEX idx_chat_file_url ON chat_messages(file_url);

-- Chat Sessions Table (for typing status and online users)
CREATE TABLE chat_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    is_typing BOOLEAN NOT NULL DEFAULT FALSE,
    is_online BOOLEAN NOT NULL DEFAULT TRUE,
    last_activity TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_chat_session_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_session_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint for user-ticket combination
    UNIQUE KEY uk_chat_session_user_ticket (user_id, ticket_id)
);

-- Indexes for chat sessions
CREATE INDEX idx_chat_session_ticket ON chat_sessions(ticket_id);
CREATE INDEX idx_chat_session_user ON chat_sessions(user_id);
CREATE INDEX idx_chat_session_typing ON chat_sessions(is_typing);
CREATE INDEX idx_chat_session_online ON chat_sessions(is_online);
CREATE INDEX idx_chat_session_activity ON chat_sessions(last_activity);

-- Chat Quick Replies Table
CREATE TABLE chat_quick_replies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    department_id BIGINT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    usage_count INT NOT NULL DEFAULT 0,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_quick_reply_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_quick_reply_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    CONSTRAINT fk_quick_reply_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for quick replies
CREATE INDEX idx_quick_reply_company ON chat_quick_replies(company_id);
CREATE INDEX idx_quick_reply_department ON chat_quick_replies(department_id);
CREATE INDEX idx_quick_reply_category ON chat_quick_replies(category);
CREATE INDEX idx_quick_reply_active ON chat_quick_replies(is_active);

-- Chat Message Reactions Table
CREATE TABLE chat_message_reactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    reaction_type VARCHAR(50) NOT NULL, -- 'like', 'love', 'laugh', 'wow', 'sad', 'angry'
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_reaction_message FOREIGN KEY (message_id) REFERENCES chat_messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_reaction_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint for user-message-reaction combination
    UNIQUE KEY uk_reaction_user_message (user_id, message_id, reaction_type)
);

-- Indexes for reactions
CREATE INDEX idx_reaction_message ON chat_message_reactions(message_id);
CREATE INDEX idx_reaction_user ON chat_message_reactions(user_id);
CREATE INDEX idx_reaction_type ON chat_message_reactions(reaction_type);

-- Chat Message Mentions Table
CREATE TABLE chat_message_mentions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    mentioned_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_mention_message FOREIGN KEY (message_id) REFERENCES chat_messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_mention_user FOREIGN KEY (mentioned_user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint for message-user combination
    UNIQUE KEY uk_mention_message_user (message_id, mentioned_user_id)
);

-- Indexes for mentions
CREATE INDEX idx_mention_message ON chat_message_mentions(message_id);
CREATE INDEX idx_mention_user ON chat_message_mentions(mentioned_user_id);

-- Chat Message Attachments Table (for better file management)
CREATE TABLE chat_message_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    mime_type VARCHAR(100) NULL,
    thumbnail_url VARCHAR(500) NULL,
    is_image BOOLEAN NOT NULL DEFAULT FALSE,
    width INT NULL,
    height INT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_attachment_message FOREIGN KEY (message_id) REFERENCES chat_messages(id) ON DELETE CASCADE
);

-- Indexes for attachments
CREATE INDEX idx_attachment_message ON chat_message_attachments(message_id);
CREATE INDEX idx_attachment_type ON chat_message_attachments(file_type);
CREATE INDEX idx_attachment_image ON chat_message_attachments(is_image);

-- Insert sample quick replies
INSERT INTO chat_quick_replies (company_id, title, content, category, created_by) VALUES
(1, 'Merhaba', 'Merhaba! Size nasıl yardımcı olabilirim?', 'greeting', 1),
(1, 'Teşekkürler', 'Teşekkür ederiz, başka bir sorunuz var mı?', 'closing', 1),
(1, 'Bekleyin', 'Lütfen bekleyin, sorunuzu inceliyorum...', 'status', 1),
(1, 'Çözüldü', 'Sorununuz çözülmüştür. Başka bir yardıma ihtiyacınız var mı?', 'resolution', 1);

-- Add chat-related columns to tickets table
ALTER TABLE tickets 
ADD COLUMN last_message_at TIMESTAMP NULL,
ADD COLUMN unread_message_count INT NOT NULL DEFAULT 0,
ADD COLUMN chat_enabled BOOLEAN NOT NULL DEFAULT TRUE;

-- Index for new columns
CREATE INDEX idx_ticket_last_message ON tickets(last_message_at);
CREATE INDEX idx_ticket_unread_count ON tickets(unread_message_count);
CREATE INDEX idx_ticket_chat_enabled ON tickets(chat_enabled); 