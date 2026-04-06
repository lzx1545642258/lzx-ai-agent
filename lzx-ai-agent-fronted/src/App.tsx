import React from 'react';
import { ChatProvider, useChat } from './contexts/ChatContext';
import { AgentSelector } from './components/AgentSelector';
import { ChatInterface } from './components/ChatInterface';
import { Layout } from './components/Layout';

const AppContent: React.FC = () => {
  const { state } = useChat();

  return (
    <Layout>
      {state.currentAgent ? <ChatInterface /> : <AgentSelector />}
    </Layout>
  );
};

const App: React.FC = () => {
  return (
    <ChatProvider>
      <AppContent />
    </ChatProvider>
  );
};

export default App;
