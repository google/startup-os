export * from './auth.service';
export * from './differenceService/difference.service';
export * from './firebaseService/firebase.service';
export * from './proto/proto.service';
export * from './proto/messages';
export * from './notification.service';
export * from './highlight.service';

import { AuthService } from './auth.service';
import { DifferenceService } from './differenceService/difference.service';
import { FirebaseService } from './firebaseService/firebase.service';
import { HighlightService } from './highlight.service';
import { NotificationService } from './notification.service';
import { ProtoService } from './proto/proto.service';

export const Services = [
  AuthService,
  DifferenceService,
  FirebaseService,
  HighlightService,
  NotificationService,
  ProtoService,
];
