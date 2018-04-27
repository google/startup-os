// The file contents for the current environment will overwrite
// these during build. The build system defaults to the dev environment
// which uses `environment.ts`, but if you do `ng build --env=prod`
// then `environment.prod.ts` will be used instead. The list of which
//  env maps to which file can be found in `.angular-cli.json`.

export const environment = {
  production: false,
  firebase: {
    apiKey: 'AIzaSyCGyjBhcLuGKuQeYueDj-Ql433Vnhnhyvs',
    authDomain: 'code-reviews-ca6c1.firebaseapp.com',
    databaseURL: 'https://code-reviews-ca6c1.firebaseio.com',
    projectId: 'code-reviews-ca6c1',
    storageBucket: 'code-reviews-ca6c1.appspot.com',
    messagingSenderId: '372884956631'
  },
  aaStorageconfig: {
    apiKey: 'AIzaSyC_na_vn9d5Fi2sHrBKp1H6UD_0QIT8r3c',
    authDomain: 'aa-storage.firebaseapp.com',
    databaseURL: 'https://aa-storage.firebaseio.com',
    projectId: 'aa-storage',
    storageBucket: 'aa-storage.appspot.com',
    messagingSenderId: '12109533178'
  }
};
