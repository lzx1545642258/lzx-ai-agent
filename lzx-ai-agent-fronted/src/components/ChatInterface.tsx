import React from 'react';
import { ArrowLeft, Heart, TestTube2 } from 'lucide-react';
import { useChat } from '../contexts/ChatContext';
import { MessageList } from './MessageList';
import { MessageInput } from './MessageInput';

const agentInfo = {
  love: { name: '恋爱大师', icon: <Heart className="w-6 h-6 text-pink-500" /> },
  test: { name: '测试智能体', icon: <TestTube2 className="w-6 h-6 text-blue-500" /> },
};

export const ChatInterface: React.FC = () => {
  const { state, sendMessage, goBack } = useChat();

  if (!state.currentAgent) return null;

  const agent = agentInfo[state.currentAgent];

  return (
    <div className="h-screen flex flex-col bg-gray-50">
      <header className="bg-white border-b border-gray-200 px-4 py-3 flex items-center gap-4">
        <button
          onClick={goBack}
          className="p-2 hover:bg-gray-100 rounded-full transition-colors"
        >
          <ArrowLeft className="w-6 h-6 text-gray-600" />
        </button>
        <div className="flex items-center gap-3">
          {agent.icon}
          <h2 className="text-xl font-bold text-gray-900">{agent.name}</h2>
        </div>
        {state.error && (
          <div className="ml-auto text-red-500 text-sm">{state.error}</div>
        )}
      </header>

      <MessageList messages={state.messages} isStreaming={state.isStreaming} />
      <MessageInput onSend={sendMessage} disabled={state.isStreaming || state.isLoading} />
    </div>
  );
};
