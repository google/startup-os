import { Injectable } from '@angular/core';
import * as hljs from 'highlight.js';

@Injectable()
export class HighlightService {
  highlight(code: string, language: string): string {
    return hljs.highlight(language, code, true).value;
  }
}
