import React, { createContext, useContext, useReducer, ReactNode, useCallback } from 'react';
import { ChatState, Message } from '../types';
import { chatWithAgent } from '../services/api';

interface ChatContextType {
  state: ChatState;
  selectAgent: (agent: 'love' | 'test') => void;
  sendMessage: (message: string) => void;
  goBack: () => void;
}

const ChatContext = createContext<ChatContextType | undefined>(undefined);

type Action =
  | { type: 'SELECT_AGENT'; payload: 'love' | 'test' }
  | { type: 'ADD_MESSAGE'; payload: Message }
  | { type: 'UPDATE_STREAMING_MESSAGE'; payload: { id: string; content: string } }
  | { type: 'COMPLETE_STREAMING_MESSAGE'; payload: string }
  | { type: 'SET_LOADING'; payload: boolean }
  | { type: 'SET_STREAMING'; payload: boolean }
  | { type: 'SET_ERROR'; payload: string | null }
  | { type: 'GO_BACK' };

const initialState: ChatState = {
  currentAgent: null,
  chatId: '',
  messages: [],
  isLoading: false,
  isStreaming: false,
  streamingMessageId: null,
  error: null,
};

const chatReducer = (state: ChatState, action: Action): ChatState => {
  switch (action.type) {
    case 'SELECT_AGENT':
      return {
        ...state,
        currentAgent: action.payload,
        chatId: Date.now().toString(),
        messages: [],
        error: null,
      };
    case 'ADD_MESSAGE':
      return {
        ...state,
        messages: [...state.messages, action.payload],
      };
    case 'UPDATE_STREAMING_MESSAGE':
      return {
        ...state,
        messages: state.messages.map((msg) =>
          msg.id === action.payload.id
            ? { ...msg, content: action.payload.content }
            : msg
        ),
      };
    case 'COMPLETE_STREAMING_MESSAGE':
      return {
        ...state,
        messages: state.messages.map((msg) =>
          msg.id === action.payload ? { ...msg, isComplete: true } : msg
        ),
        streamingMessageId: null,
        isStreaming: false,
      };
    case 'SET_LOADING':
      return { ...state, isLoading: action.payload };
    case 'SET_STREAMING':
      return { ...state, isStreaming: action.payload };
    case 'SET_ERROR':
      return { ...state, error: action.payload, isLoading: false, isStreaming: false };
    case 'GO_BACK':
      return {
        ...state,
        currentAgent: null,
        messages: [],
        error: null,
      };
    default:
      return state;
  }
};

export const ChatProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [state, dispatch] = useReducer(chatReducer, initialState);

  const selectAgent = useCallback((agent: 'love' | 'test') => {
    dispatch({ type: 'SELECT_AGENT', payload: agent });
  }, []);

  const sendMessage = useCallback(
    (message: string) => {
      if (!state.currentAgent || state.isStreaming || state.isLoading) return;

      dispatch({ type: 'SET_ERROR', payload: null });

      const userMessageId = Date.now().toString();
      const userMessage: Message = {
        id: userMessageId,
        content: message,
        sender: 'user',
        timestamp: Date.now(),
        isComplete: true,
      };
      dispatch({ type: 'ADD_MESSAGE', payload: userMessage });

      const agentMessageId: string = (Date.now() + 1).toString();
      const agentMessage: Message = {
        id: agentMessageId,
        content: '',
        sender: 'agent',
        timestamp: Date.now(),
        isComplete: false,
      };
      dispatch({ type: 'ADD_MESSAGE', payload: agentMessage });
      dispatch({ type: 'SET_STREAMING', payload: true });

      let accumulatedContent = '';

      chatWithAgent(
        state.currentAgent,
        message,
        state.chatId,
        (content) => {
          accumulatedContent += content;
          dispatch({
            type: 'UPDATE_STREAMING_MESSAGE',
            payload: { id: agentMessageId, content: accumulatedContent },
          });
        },
        () => {
          dispatch({ type: 'COMPLETE_STREAMING_MESSAGE', payload: agentMessageId });
        },
        (error) => {
          dispatch({ type: 'SET_ERROR', payload: '连接失败，请稍后重试' });
        }
      );
    },
    [state.currentAgent, state.chatId, state.isStreaming, state.isLoading]
  );

  const goBack = useCallback(() => {
    dispatch({ type: 'GO_BACK' });
  }, []);

  return (
    <ChatContext.Provider
      value={{
        state,
        selectAgent,
        sendMessage,
        goBack,
      }}
    >
      {children}
    </ChatContext.Provider>
  );
};

export const useChat = () => {
  const context = useContext(ChatContext);
  if (!context) {
    throw new Error('useChat must be used within a ChatProvider');
  }
  return context;
};
