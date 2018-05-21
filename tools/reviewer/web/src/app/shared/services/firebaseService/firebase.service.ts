import { Comment, Diff } from '@/shared';
import { Injectable } from '@angular/core';
import {
  AngularFireAction,
  AngularFireDatabase,
  AngularFireObject,
  DatabaseSnapshot
} from 'angularfire2/database';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class FirebaseService {
  constructor(private db: AngularFireDatabase) {}

  getDiffs(): Observable<any> {
    return this.db.object('diffs').valueChanges();
  }

  getDiff(id: string): Observable<Diff> {
    const obj: AngularFireObject<Diff> = this.db.object('diffs/' + id);
    return obj.valueChanges();
  }

  // TODO: add/delete/change separete elements,
  // instead of updating whole diff
  updateDiff(diff: Diff): Observable<void> {
    return new Observable(observer => {
      this.db
        .object('diffs/' + diff.number)
        .update(diff)
        .then(() => {
          observer.next();
        })
        .catch(err => {
          observer.error();
        });
    });
  }

  removeProperty(diff: Diff, property: string): Promise<void> {
    return this.db.object('diffs/' + diff.number + '/' + property).remove();
  }
}
