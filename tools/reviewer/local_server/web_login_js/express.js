const express = require('express');

const app = express();

app.use(express.static('./src'), (req, res, next) => {
  next();
});

const port = 4000;
const server = app.listen(port, () => {
  console.log('Express is started: ' + port);
});
