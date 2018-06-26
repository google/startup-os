import { Injectable } from '@angular/core';

@Injectable()
export class DifferenceService {
  compare(oldCode: string, newCode: string): number[] {
    const changes: number[] = [];
    const oldLines: string[] = oldCode.split('\n');
    const newLines: string[] = newCode.split('\n');

    // TODO: Use localserver response instead.

    const minLength: number = Math.min(oldLines.length, newLines.length);
    for (let i = 0; i < minLength; i++) {
      if (oldLines[i] !== newLines[i]) {
        changes.push(i);
      }
    }

    return changes;
  }
}
