import React, { useState, useRef } from 'react';
import { Send, Paperclip, Smile } from 'lucide-react';

interface ChatInputProps {
    onSendMessage: (message: string) => void;
    onTyping?: (isTyping: boolean) => void;
    disabled?: boolean;
    placeholder?: string;
}

const ChatInput: React.FC<ChatInputProps> = ({
    onSendMessage,
    onTyping,
    disabled = false,
    placeholder = "Mesajınızı yazın..."
}) => {
    const [message, setMessage] = useState('');
    const [isTyping, setIsTyping] = useState(false);
    const textareaRef = useRef<HTMLTextAreaElement>(null);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (message.trim() && !disabled) {
            onSendMessage(message.trim());
            setMessage('');
            setIsTyping(false);
            onTyping?.(false);
        }
    };

    const handleKeyPress = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSubmit(e);
        }
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        const newMessage = e.target.value;
        setMessage(newMessage);

        // Handle typing indicator
        if (onTyping) {
            const wasTyping = isTyping;
            const isNowTyping = newMessage.length > 0;

            if (wasTyping !== isNowTyping) {
                setIsTyping(isNowTyping);
                onTyping(isNowTyping);
            }
        }
    };

    const handleFileUpload = () => {
        // TODO: Implement file upload functionality
        console.log('File upload clicked');
    };

    const handleEmojiPicker = () => {
        // TODO: Implement emoji picker
        console.log('Emoji picker clicked');
    };

    return (
        <div className="border-t border-gray-200 p-4 bg-white">
            <form onSubmit={handleSubmit} className="flex items-end space-x-2">
                <div className="flex-1 relative">
                    <textarea
                        ref={textareaRef}
                        value={message}
                        onChange={handleInputChange}
                        onKeyPress={handleKeyPress}
                        placeholder={placeholder}
                        disabled={disabled}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50"
                        rows={1}
                        style={{ minHeight: '40px', maxHeight: '120px' }}
                    />
                </div>

                <div className="flex items-center space-x-1">
                    <button
                        type="button"
                        onClick={handleFileUpload}
                        disabled={disabled}
                        className="p-2 text-gray-500 hover:text-gray-700 disabled:opacity-50"
                        title="Dosya ekle"
                    >
                        <Paperclip className="w-5 h-5" />
                    </button>

                    <button
                        type="button"
                        onClick={handleEmojiPicker}
                        disabled={disabled}
                        className="p-2 text-gray-500 hover:text-gray-700 disabled:opacity-50"
                        title="Emoji"
                    >
                        <Smile className="w-5 h-5" />
                    </button>

                    <button
                        type="submit"
                        disabled={disabled || !message.trim()}
                        className="p-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                        title="Gönder"
                    >
                        <Send className="w-5 h-5" />
                    </button>
                </div>
            </form>
        </div>
    );
};

export default ChatInput;