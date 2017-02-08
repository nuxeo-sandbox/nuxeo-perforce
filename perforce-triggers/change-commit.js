#!/usr/bin/env node

'use strict';

const _ = require('lodash');
const Nuxeo = require('nuxeo');
const s = require('synchronize');
const os = require('os');
const exec = require('child_process').execSync;

const P4CLIENT = process.env.P4CLIENT || '/usr/local/bin/p4';
const NUXEO_HOST = process.env.NUXEO_HOST || 'http://localhost:8080/nuxeo';
const NUXEO_USER = process.env.NUXEO_USER || 'Administrator';
const NUXEO_PWD = process.env.NUXEO_PWD || 'Administrator';
const OPERATION = 'Document.VCSEventsReceiverOperation';

//Usage: $0 %change% %serverport%
//Sample Commande: $0 $change localhost:1666

// Ensure args are correct
if (process.argv.length < 4) {
  console.log(process.argv.join(', '));
  console.log('Usage: $0 %change% %serverport%');
  process.exit(1);
}

// Ensure Nuxeo Client can authenticate
const nuxeo = new Nuxeo({
  baseURL: NUXEO_HOST,
  auth: {
    method: 'basic',
    username: NUXEO_USER,
    password: NUXEO_PWD
  }
});

s.fiber(() => {
  // Try to fetch Root documen to ensure everything is ok
  sync((cb) => {
    nuxeo.repository().fetch('/').then(() => {
      // nothing to do, server is available
      cb();
    }).catch(err => {
      console.log(`${err.name}: ${err.message}`);
      process.exit(1);
    });
  });

  const args = process.argv.splice(2);
  const change = args[0];
  const server = args[1];
  const res = exec(`${P4CLIENT} -p ${server} describe -s ${change}`, {
    encoding: 'UTF-8'
  });

  const lines = res.split(os.EOL);
  const changedFilesIndex = _(lines).findIndex(line => {
    return line.match(/^affected files/i);
  });
  if (changedFilesIndex <= 0) {
    // No changed files
    process.exit(0);
  }

  const files = _(lines).drop(changedFilesIndex).map(line => {
    // format is:
    // //depot/src/main/dasd... //Nuxeo/main/dasd#3 delete
    var m = line.match(/^(.*)\.\.\. (.+)#(\d+) (?:move\/)?(.+)/i);
    return m ? {
      filePath: m[1] || m[2],
      change: m[3],
      action: m[4]
    } : null;
  }).filter(o => {
    // Filter not matching lines (blank lines for instance)
    return o;
  }).value();

  // Execute synchronously a Operation call per files changed
  const errs = [];
  _(files).each((params) => {
    sync((cb) => {
      nuxeo.operation(OPERATION)
        .params(_.extend(params, {
          provider: 'perforce'
        }))
        .execute({
          headers: {
            'X-NXVoidOperation': true
          }
        })
        .then(() => {
          console.log(`INFO: ${params.filePath}#${params.change} ${params.action} correctly pushed to Nuxeo.`);
          cb();
        })
        .catch((err) => {
          errs.push({
            error: err,
            params: params
          });
          cb();
        });
    });
  });

  _(errs).each((err) => {
    console.log(err);
  });
});

function sync(func) {
  return s.await(func(s.defer()));
}
