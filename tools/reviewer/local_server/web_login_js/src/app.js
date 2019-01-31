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

  init() {
    if (!this.isChrome()) {
      this.pleaseOpenChrome();
    }
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

  isChrome() {
    return /Chrome/.test(navigator.userAgent) && /Google Inc/.test(navigator.vendor);
  }

  pleaseOpenChrome() {
    document.getElementById('chrome').style.display = 'block';
  }
}
const app = new App();

window.onload = () => {
  app.init();
};
