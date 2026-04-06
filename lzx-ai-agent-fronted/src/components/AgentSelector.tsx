import React from 'react';
import { Heart, TestTube2 } from 'lucide-react';
import { useChat } from '../contexts/ChatContext';
import { Agent } from '../types';

const agents: Agent[] = [
  {
    id: 'love',
    name: '恋爱大师',
    description: '专业的恋爱顾问，帮你解决情感问题',
    icon: '❤️',
    color: 'from-pink-400 to-red-500',
  },
  {
    id: 'test',
    name: '测试智能体',
    description: '用于测试功能的智能助手',
    icon: '🧪',
    color: 'from-blue-400 to-indigo-500',
  },
];

export const AgentSelector: React.FC = () => {
  const { selectAgent } = useChat();

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4">
      <div className="text-center mb-12">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">AI 智能体聊天</h1>
        <p className="text-gray-600 text-lg">选择一个智能体开始对话</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-2xl w-full">
        {agents.map((agent) => (
          <button
            key={agent.id}
            onClick={() => selectAgent(agent.id)}
            className="group bg-white rounded-2xl p-8 shadow-lg hover:shadow-2xl transition-all duration-300 hover:-translate-y-1 border-2 border-transparent hover:border-blue-200"
          >
            <div className={`w-16 h-16 rounded-xl bg-gradient-to-br ${agent.color} flex items-center justify-center text-3xl mb-4 mx-auto group-hover:scale-110 transition-transform`}>
              {agent.id === 'love' ? <Heart className="w-8 h-8 text-white" /> : <TestTube2 className="w-8 h-8 text-white" />}
            </div>
            <h3 className="text-xl font-bold text-gray-900 mb-2">{agent.name}</h3>
            <p className="text-gray-600">{agent.description}</p>
          </button>
        ))}
      </div>
    </div>
  );
};
