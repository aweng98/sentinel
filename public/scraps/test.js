/**
 * Created by marco on 2/16/15.
 */


var foo = {
    a: 1,
    b: 2
};

var bar = {
    c: 99
};

for (k in foo) {
    console.log(k, foo[k]);
    bar[k] = foo[k];
}

console.log(bar);
delete bar.b;
console.log(bar);

var many = [];

if (many.length === 0) {
    console.log('empty');
}