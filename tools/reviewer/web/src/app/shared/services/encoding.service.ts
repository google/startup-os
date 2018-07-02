import { Injectable } from '@angular/core';

@Injectable()
export class EncodingService {
  encodeUint8ArrayToBase64String(binary: Uint8Array): string {
    return btoa(String.fromCharCode.apply(null, binary));
  }

  decodeBase64StringToUint8Array(base64: string): Uint8Array {
    const raw: string = window.atob(base64);
    const uint8Array = new Uint8Array(new ArrayBuffer(raw.length));
    for (let i = 0; i < raw.length; i++) {
      uint8Array[i] = raw.charCodeAt(i);
    }
    return uint8Array;
  }
}
