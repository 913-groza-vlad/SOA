import { Client } from '@stomp/stompjs';

const WS_BASE =
  import.meta.env.VITE_WS_BASE_URL || 'ws://localhost:4000/ws/notifications';

export function createNotificationClient(userId, onMessage) {
  if (!userId) return null;

  const client = new Client({
    brokerURL: WS_BASE, // direct WS URL
    reconnectDelay: 5000,
    debug: (str) => {
      console.log('[STOMP]', str);
    },
    onConnect: () => {
      console.log('STOMP connected');
      // subscribe to user-specific topic
      const destination = `/topic/notifications/${userId}`;
      client.subscribe(destination, (message) => {
        try {
          const body = JSON.parse(message.body);
          onMessage && onMessage(body);
        } catch (e) {
          console.error('Failed to parse notification message', e);
        }
      });
    },
    onStompError: (frame) => {
      console.error('Broker reported error:', frame.headers['message']);
      console.error('Additional details:', frame.body);
    },
  });

  client.activate();
  return client;
}
