(this["webpackJsonpjamal-debug-ui"]=this["webpackJsonpjamal-debug-ui"]||[]).push([[0],{19:function(e,t,n){},20:function(e,t,n){},22:function(e,t,n){},23:function(e,t,n){},42:function(e,t,n){"use strict";n.r(t);var c=n(2),r=n.n(c),a=n(14),s=n.n(a),o=(n(19),n(4)),i=(n(20),n(0)),u=function(e){var t=e.text,n=e.macro,c=void 0===n?"":n,r=t.indexOf(c),a=r+c.length,s=-1===r?t:t.substr(0,r),o=-1===r?"":c,u=-1===r?"":t.substr(a);return Object(i.jsxs)("pre",{className:"Input_SourceCode",children:[Object(i.jsx)("span",{children:s}),Object(i.jsx)("span",{className:"red",children:o}),Object(i.jsx)("span",{children:u})]})},j=(n(22),function(e){var t=e.title,n=e.onClickHandler;return Object(i.jsx)(i.Fragment,{children:Object(i.jsx)("button",{onClick:n,children:t})})}),l=(n(23),n(3)),b=n.n(l);var h=function(){var e=Object(c.useState)(""),t=Object(o.a)(e,2),n=t[0],r=t[1],a=Object(c.useState)(""),s=Object(o.a)(a,2),l=s[0],h=s[1],d=Object(c.useState)(""),p=Object(o.a)(d,2),O=p[0];return p[1],Object(c.useEffect)((function(){b.a.get("http://localhost:8080/inputBefore").then((function(e){return r(e.data)})),b.a.get("http://localhost:8080/processing").then((function(e){return h(e.data)}))})),Object(i.jsx)("div",{className:"App",children:Object(i.jsxs)("header",{className:"App-header",children:[Object(i.jsx)(j,{title:"Run",onClickHandler:function(){b.a.post("http://localhost:8080/run").then((function(){return console.log("OK")}))}}),Object(i.jsx)(u,{text:n,macro:l}),Object(i.jsx)("p",{}),Object(i.jsx)(u,{text:O}),Object(i.jsx)("a",{className:"App-link",href:"https://reactjs.org",target:"_blank",rel:"noopener noreferrer",children:"Learn React"})]})})},d=function(e){e&&e instanceof Function&&n.e(3).then(n.bind(null,43)).then((function(t){var n=t.getCLS,c=t.getFID,r=t.getFCP,a=t.getLCP,s=t.getTTFB;n(e),c(e),r(e),a(e),s(e)}))};s.a.render(Object(i.jsx)(r.a.StrictMode,{children:Object(i.jsx)(h,{})}),document.getElementById("root")),d()}},[[42,1,2]]]);
//# sourceMappingURL=main.a1b259d4.chunk.js.map