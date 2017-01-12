#!/usr/bin/env node

'use strict';

const Nuxeo = require('nuxeo');

const NUXEO_HOST = process.env.NUXEO_HOST || 'http://localhost:8080/nuxeo';
const NUXEO_USER = process.env.NUXEO_USER || 'Administrator';
const NUXEO_PWD = process.env.NUXEO_PWD || 'Administrator';
const OPERATION = 'Perforce.Initializer';

const nuxeo = new Nuxeo({
  baseURL: NUXEO_HOST,
  auth: {
    method: 'basic',
    username: NUXEO_USER,
    password: NUXEO_PWD
  }
});

nuxeo.operation(OPERATION)
  .execute({
    headers: {
      'X-NXVoidOperation': true
    }
  })
  .then(() => {
    console.log('Perforce initializer launched.');
  })
  .catch((err) => {
    throw err;
  });
