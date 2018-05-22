import { Injectable } from '@angular/core';

@Injectable()
export class DifferenceService {
  compare(oldCode: string, newCode: string): number[] {
    const changes: number[] = [];
    const oldLines: string[] = oldCode.split('\n');
    const newLines: string[] = newCode.split('\n');
    if (oldLines.length !== newLines.length) {
      // TODO: add multiline changes supporting
      throw new Error('Multiline changes are not supported.');
    }
    oldLines.forEach((v, i) => {
      if (oldLines[i] !== newLines[i]) {
        changes.push(i);
      }
    });
    return changes;
  }
}
