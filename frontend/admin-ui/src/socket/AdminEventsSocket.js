import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_HTTP_URL = `${window.location.protocol}//${window.location.host}/ws/admin-events`;

export function createAdminEventsClient(onEvent) {
  const client = new Client({
    webSocketFactory: () => new SockJS(WS_HTTP_URL),
    reconnectDelay: 5000,
    debug: () => {
      // console.log(str);
    },
  });

  client.onConnect = () => {
    client.subscribe('/topic/admin/events', (message) => {
      try {
        const event = JSON.parse(message.body);
        onEvent(event);
      } catch (e) {
        console.error('Failed to parse admin event', e);
      }
    });
  };

  client.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
  };

  return client;
}
