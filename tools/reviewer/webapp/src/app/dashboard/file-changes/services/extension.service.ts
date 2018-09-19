import { Injectable } from '@angular/core';

@Injectable()
export class ExtensionService {
  // Get langulage from filename. Example:
  // filename.js -> javascript
  getLanguage(filename: string): string {
    const extensionRegExp: RegExp = /(?:\.([^.]+))?$/;
    const extension: string = extensionRegExp.exec(filename)[1];

    switch (extension) {
      case 'js': return 'javascript';
      case 'ts': return 'typescript';
      case 'java': return 'java';
      case 'proto': return 'protobuf';
      case 'md': return 'markdown';
      case 'json': return 'json';
      case 'css': return 'css';
      case 'scss': return 'scss';
      case 'html': return 'html';
      case 'sh': return 'bash';
      case 'xml': return 'xml';
      case 'py': return 'python';

      default: return 'clean';
    }

    // All supported languages:
    // https://github.com/highlightjs/highlight.js/tree/master/src/languages
  }
}
