module.exports =  {
    env: {
        browser: true,
        jest: true,
        node: true,
    },
    parser:  '@typescript-eslint/parser',  // Specifies the ESLint parser
    extends:  [
      'eslint:recommended',
      'plugin:@typescript-eslint/recommended',  // Uses the recommended rules from the @typescript-eslint/eslint-plugin
    ],
    globals: {
        JSX: true,
    },
    parserOptions:  {
      ecmaVersion:  2018,  // Allows for the parsing of modern ECMAScript features
      sourceType:  'module',  // Allows for the use of imports
    },
    plugins: [
        'react',
        '@typescript-eslint'
    ],
    rules:  {
        "curly": 1,
        "@typescript-eslint/explicit-function-return-type": [0],
        "@typescript-eslint/no-explicit-any": [0],
        "ordered-imports": [0],
        "object-literal-sort-keys": [0],
        "max-len": [1, 120],
        "new-parens": 1,
        "no-bitwise": 1,
        "no-cond-assign": 1,
        "no-trailing-spaces": 0,
        "eol-last": 1,
        "func-style": ["error", "declaration", { "allowArrowFunctions": true }],
        "semi": 1,
        "no-var": 0,
        'indent': 'off',
        'linebreak-style': 0,
        'no-console': 0,
        'jsx-quotes': [2, 'prefer-single'],
        'quotes': ['error', 'single']
    },
  };
