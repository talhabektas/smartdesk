import React from 'react';

interface ChatTypingIndicatorProps {
    isTyping: boolean;
    userName?: string;
}

const ChatTypingIndicator: React.FC<ChatTypingIndicatorProps> = ({
    isTyping,
    userName = "Birisi"
}) => {
    if (!isTyping) return null;

    return (
        <div className="flex items-center space-x-2 p-3 text-sm text-gray-500">
            <div className="flex space-x-1">
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
            </div>
            <span>{userName} yazıyor...</span>
        </div>
    );
};

export default ChatTypingIndicator; 