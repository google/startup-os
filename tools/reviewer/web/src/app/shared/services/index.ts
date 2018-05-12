export * from './auth.service';
export * from './differenceService/difference.service';
export * from './firebaseService/firebase.service';
export * from './proto/proto.service';
export * from './proto/messages';
export * from './notification.service';

import { AuthGuard } from '@/shared/services/auth.guard';
import { AuthService } from './auth.service';
import { DifferenceService } from './differenceService/difference.service';
import { FirebaseService } from './firebaseService/firebase.service';
import { NotificationService } from './notification.service';
import { ProtoService } from './proto/proto.service';

export const Services = [
  AuthGuard,
  AuthService,
  DifferenceService,
  FirebaseService,
  NotificationService,
  ProtoService
];
