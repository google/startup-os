// `tslib` must be imported explicitly into the application
// when building angular from source using bazel
// TODO(gmagolan): Remove this import once it is no longer needed.
// See https://github.com/bazelbuild/rules_typescript/issues/252
import 'tslib';

import {platformBrowser} from '@angular/platform-browser';
import {AppModuleNgFactory} from './app.module.ngfactory';

platformBrowser().bootstrapModuleFactory(AppModuleNgFactory);
