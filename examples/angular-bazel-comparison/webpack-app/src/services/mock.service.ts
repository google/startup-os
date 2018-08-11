import { Injectable } from '@angular/core';

@Injectable()
export class MockService {
  private data: string;

  getData(): string {
    return this.data;
  }

  setData(data: string): void {
    this.data = data;
  }
}
