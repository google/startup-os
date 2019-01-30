const config = {
  apiKey: 'AIzaSyAn-A7YXlqR2JNOvlbhkVwcnsOigmphQIw',
  authDomain: 'startupos-5f279.firebaseapp.com',
  databaseURL: 'https://startupos-5f279.firebaseio.com',
  projectId: 'startupos-5f279',
  storageBucket: 'startupos-5f279.appspot.com',
  messagingSenderId: '160348327132'
};
const port = 7000;

class App {
  constructor() {
    this.firebaseApp = firebase.initializeApp(config);
    this.firebaseApp.auth().onAuthStateChanged(userData => {
      const isLogin = !!userData;
      if (isLogin) {
        this.displayElements(false, true);
      } else {
        this.displayElements(true, false);
      }
    });
  }

  login() {
    const provider = new firebase.auth.GoogleAuthProvider();
    this.firebaseApp.auth()
      .signInWithPopup(provider)
      .then(result => {
        this.getJwtToken(result.user.refreshToken);
      })
  }

  logout() {
    this.firebaseApp.auth().signOut();
  }

  getJwtToken(refreshToken) {
    this.firebaseApp.auth().currentUser.getIdToken(true)
      .then(jwtToken => {
        this.sendToServer(jwtToken, refreshToken);
      });
  }

  sendToServer(jwtToken, refreshToken) {
    const url = `http://localhost:${port}/token`;
    const data = {
      projectId: config.projectId,
      apiKey: config.apiKey,
      jwtToken: jwtToken,
      refreshToken: refreshToken,
    };
    const http = new XMLHttpRequest();
    http.open('POST', url, true);
    http.setRequestHeader('Content-type', 'application/json');
    http.send(JSON.stringify(data));
  }

  displayElements(login, logout) {
    const loginElement = document.getElementById('login');
    const logoutElement = document.getElementById('logout');
    loginElement.style.display = login ? 'block' : 'none';
    logoutElement.style.display = logout ? 'block' : 'none';
  }
}
const app = new App();
