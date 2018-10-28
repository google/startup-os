import { Injectable } from '@angular/core';
import * as hljs from 'highlight.js';

@Injectable()
export class HighlightService {
  highlight(code: string, language: string): string {
    return hljs.highlight(language, code, true).value;
  }

  // Replace html special chars with html entities
  htmlSpecialChars(code: string): string {
    const findSpecialChars: RegExp = /[&<>"'`=\/]/g;
    return code.replace(findSpecialChars, char => {
      switch (char) {
        case '&': return '&amp;';
        case '<': return '&lt;';
        case '>': return '&gt;';
        case '"': return '&quot;';
        case "'": return '&#39;';
        case '/': return '&#x2F;';
        case '`': return '&#x60;';
        case '=': return '&#x3D;';
        default: return char;
      }
    });
  }
}
