export interface Message {
  id: string;
  content: string;
  sender: 'user' | 'agent';
  timestamp: number;
  isComplete: boolean;
}

export interface ChatState {
  currentAgent: 'love' | 'test' | null;
  chatId: string | number;
  messages: Message[];
  isLoading: boolean;
  isStreaming: boolean;
  streamingMessageId: string | null;
  error: string | null;
}

export interface Agent {
  id: 'love' | 'test';
  name: string;
  description: string;
  icon: string;
  color: string;
}
