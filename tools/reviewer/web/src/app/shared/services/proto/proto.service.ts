import { Injectable } from '@angular/core';
import { load, Message, Root, Type } from 'protobufjs';
import { Observable } from 'rxjs/Observable';
import { Diff } from './messages';

// TODO: use protoc

@Injectable()
export class ProtoService {
  // Temporarily we're using a copy of proto file
  // TODO get the original proto file when it is updated
  url = 'assets/messages.proto';
  root: Root;
  Diff: Type;
  open: Observable<any>;
  constructor() {
    this.open = new Observable(observer => {
      load(this.url, (error, root) => {
        if (error) {
          observer.next(error);
          return;
        }

        this.root = root;
        this.Diff = root.lookupType('messages.Diff');

        observer.next(null);
      });
    });
    this.open.subscribe(() => { });
  }

  verify(res: any): any {
    const verifyErrorMsg = this.Diff.verify(res);
    if (verifyErrorMsg) {
      return {};
    }
    return res;
  }

  createDiff(diff: Diff): Diff {
    // tslint:disable-next-line
    return <Diff>(<any>this.Diff.fromObject(diff));
  }
}
