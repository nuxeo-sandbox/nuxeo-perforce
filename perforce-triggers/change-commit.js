#!/usr/bin/env node
//Usage: $0 %change% %serverport%
const tmp = '/tmp/perforce';
const rand = Math.random().toString(36).slice(-10);

const exec = require('child_process').execSync;
if (process.argv.length < 4) {
  console.log(process.argv.join(', '));
  console.log('Usage: $0 %change% %serverport%');
  process.exit(1);
}

const args = process.argv.splice(2);
const change = args[0];
const server = args[1];
const res = exec(`/Users/arnaud/Nuxeo/tmp/perforce/p4 -p ${server} describe -s ${change}`);

exec(`set > ${tmp}/${rand}-set`, {});
exec(`env > ${tmp}/${rand}-env`, {});
exec(`echo '${res}' > ${tmp}/${rand}-cmd`, {});