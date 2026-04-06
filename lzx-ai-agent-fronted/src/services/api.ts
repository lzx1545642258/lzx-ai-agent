export const chatWithAgent = (
  agent: 'love' | 'test',
  message: string,
  chatId: string | number,
  onMessage: (content: string) => void,
  onComplete: () => void,
  onError: (error: Event) => void
) => {
  const url = `http://localhost:8123/api/${agent}/chat?chatId=${chatId}&userMessage=${encodeURIComponent(message)}`;
  const eventSource = new EventSource(url);
  let isCompleted = false;
  let hasReceivedMessage = false;

  eventSource.onmessage = (event) => {
    console.log('SSE message received:', event.data);
    
    if (event.data === '[DONE]') {
      console.log('SSE stream completed with [DONE]');
      isCompleted = true;
      eventSource.close();
      onComplete();
      return;
    }

    hasReceivedMessage = true;
    
    try {
      const data = JSON.parse(event.data);
      onMessage(data.response);
    } catch (error) {
      onMessage(event.data);
    }
  };

  eventSource.onerror = (error) => {
    console.log('SSE error triggered, isCompleted:', isCompleted, 'hasReceivedMessage:', hasReceivedMessage);
    eventSource.close();
    
    if (isCompleted) {
      console.log('Ignoring error - stream already completed');
      return;
    }
    
    if (hasReceivedMessage) {
      console.log('Stream ended after receiving messages - treating as complete');
      onComplete();
    } else {
      console.log('Stream error before receiving any messages');
      onError(error);
    }
  };

  return () => {
    eventSource.close();
  };
};
