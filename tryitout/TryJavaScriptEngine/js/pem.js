/* Generated from Java with JSweet 2.0.0 - http://www.jsweet.org */
var PemBlock = (function () {
    function PemBlock(p) {
        var _this = this;
        this.BLOCK_CIPHER = "CIPHER";
        this.METHOD = "Method";
        this.BLOCK_SIGN = "SIGNATURE";
        this.BLOCK_PRIV = "PRIVATE KEY";
        this.BLOCK_PUB = "PUBLIC KEY";
        this.LENGTH = "Length";
        this.FORMAT = "Format";
        this.IDENT = "Ident";
        this.STRING_ENCODING = "Encoding";
        this.PRIV_ID = "PrivateKey";
        this.PUB_ID = "PublicKey";
        this.KEY_ID = "Key";
        this.SYMMETRIC = "Symmetric";
        this.DESCRIPTION = "Description";
        this.CREATED = "Created";
        this.ENCRYPTED = "Encrypted";
        this.ENC_BLOWFISH = "blowfish";
        this.BLOCK_HASH = "HASH";
        this.EMBEDDED = "Embedded";
        this.BLOCK_CONTENT = "CONTENT";
        /*private*/ this.list = ({});
        if (((typeof p === 'string') || p === null)) {
            var __args = Array.prototype.slice.call(arguments);
            this.name = null;
            this.block = null;
            this.rest = null;
            this.BLOCK_CIPHER = "CIPHER";
            this.METHOD = "Method";
            this.BLOCK_SIGN = "SIGNATURE";
            this.BLOCK_PRIV = "PRIVATE KEY";
            this.BLOCK_PUB = "PUBLIC KEY";
            this.LENGTH = "Length";
            this.FORMAT = "Format";
            this.IDENT = "Ident";
            this.STRING_ENCODING = "Encoding";
            this.PRIV_ID = "PrivateKey";
            this.PUB_ID = "PublicKey";
            this.KEY_ID = "Key";
            this.SYMMETRIC = "Symmetric";
            this.DESCRIPTION = "Description";
            this.CREATED = "Created";
            this.ENCRYPTED = "Encrypted";
            this.ENC_BLOWFISH = "blowfish";
            this.BLOCK_HASH = "HASH";
            this.EMBEDDED = "Embedded";
            this.BLOCK_CONTENT = "CONTENT";
            this.list = ({});
            this.name = null;
            this.block = null;
            this.rest = null;
            (function () {
                _this.parse(p);
            })();
        }
        else if (p === undefined) {
            var __args = Array.prototype.slice.call(arguments);
            this.name = null;
            this.block = null;
            this.rest = null;
            this.BLOCK_CIPHER = "CIPHER";
            this.METHOD = "Method";
            this.BLOCK_SIGN = "SIGNATURE";
            this.BLOCK_PRIV = "PRIVATE KEY";
            this.BLOCK_PUB = "PUBLIC KEY";
            this.LENGTH = "Length";
            this.FORMAT = "Format";
            this.IDENT = "Ident";
            this.STRING_ENCODING = "Encoding";
            this.PRIV_ID = "PrivateKey";
            this.PUB_ID = "PublicKey";
            this.KEY_ID = "Key";
            this.SYMMETRIC = "Symmetric";
            this.DESCRIPTION = "Description";
            this.CREATED = "Created";
            this.ENCRYPTED = "Encrypted";
            this.ENC_BLOWFISH = "blowfish";
            this.BLOCK_HASH = "HASH";
            this.EMBEDDED = "Embedded";
            this.BLOCK_CONTENT = "CONTENT";
            this.list = ({});
            this.name = null;
            this.block = null;
            this.rest = null;
        }
        else
            throw new Error('invalid overload');
    }
    PemBlock.prototype.getRest = function () {
        return this.rest;
    };
    PemBlock.prototype.setName = function (name) {
        this.name = name.toUpperCase();
    };
    PemBlock.prototype.getName = function () {
        return this.name;
    };
    PemBlock.prototype.getBlock = function () {
        return this.block;
    };
    /**
     *
     * @return {string}
     */
    PemBlock.prototype.toString = function () {
        var _this = this;
        var sb = { str: "", toString: function () { return this.str; } };
        /* append */ (function (sb) { sb.str = sb.str.concat("-----\n"); return sb; })(/* append */ (function (sb) { sb.str = sb.str.concat(_this.getName()); return sb; })(/* append */ (function (sb) { sb.str = sb.str.concat("-----BEGIN "); return sb; })(sb)));
        {
            var array851 = (function (o) { var s = []; for (var e in o)
                s.push({ k: e, v: o[e], getKey: function () { return this.k; }, getValue: function () { return this.v; } }); return s; })(this.list);
            var _loop_1 = function (index850) {
                var item = array851[index850];
                {
                    var key_1 = item.getKey().trim();
                    /* append */ (function (sb) { sb.str = sb.str.concat(": "); return sb; })(/* append */ (function (sb) { sb.str = sb.str.concat(key_1); return sb; })(sb));
                    var len_1 = key_1.length + 2;
                    var value_1 = new String(item.getValue()).toString();
                    if (len_1 + value_1.length <= PemBlock.BLOCK_WIDTH)
                        (function (sb) { sb.str = sb.str.concat('\n'); return sb; })(/* append */ (function (sb) { sb.str = sb.str.concat(item.getValue()); return sb; })(sb));
                    else {
                        /* append */ (function (sb) { sb.str = sb.str.concat('\n'); return sb; })(/* append */ (function (sb) { sb.str = sb.str.concat(value_1.substring(0, PemBlock.BLOCK_WIDTH - len_1)); return sb; })(sb));
                        len_1 = PemBlock.BLOCK_WIDTH - len_1;
                        while ((len_1 < value_1.length)) {
                            /* append */ (function (sb) { sb.str = sb.str.concat(' '); return sb; })(sb);
                            if (len_1 + PemBlock.BLOCK_WIDTH - 1 > value_1.length) {
                                /* append */ (function (sb) { sb.str = sb.str.concat('\n'); return sb; })(/* append */ (function (sb) { sb.str = sb.str.concat(value_1.substring(len_1)); return sb; })(sb));
                                return "break";
                            }
                            else {
                                /* append */ (function (sb) { sb.str = sb.str.concat('\n'); return sb; })(/* append */ (function (sb) { sb.str = sb.str.concat(value_1.substring(len_1, len_1 + PemBlock.BLOCK_WIDTH - 1)); return sb; })(sb));
                            }
                            len_1 = len_1 + PemBlock.BLOCK_WIDTH - 1;
                        }
                        ;
                    }
                }
            };
            for (var index850 = 0; index850 < array851.length; index850++) {
                var state_1 = _loop_1(index850);
                if (state_1 === "break")
                    break;
            }
        }
        /* append */ (function (sb) { sb.str = sb.str.concat('\n'); return sb; })(sb);
        /* append */ (function (sb) { sb.str = sb.str.concat(_this.getEncodedBlock()); return sb; })(sb);
        /* append */ (function (sb) { sb.str = sb.str.concat("\n\n"); return sb; })(sb);
        /* append */ (function (sb) { sb.str = sb.str.concat("-----\n"); return sb; })(/* append */ (function (sb) { sb.str = sb.str.concat(_this.getName()); return sb; })(/* append */ (function (sb) { sb.str = sb.str.concat("-----END "); return sb; })(sb)));
        return sb.str;
    };
    PemBlock.prototype.getEncodedBlock = function () {
        var b = PemBlock.encodeUnicode(this.block, true);
        var c = "";
        while ((b.length > PemBlock.BLOCK_WIDTH)) {
            c = c + b.substring(0, PemBlock.BLOCK_WIDTH) + '\n';
            b = b.substring(PemBlock.BLOCK_WIDTH);
        }
        ;
        c = c + b;
        return c;
    };
    PemBlock.prototype.getBytesBlock = function () {
    		return atob(this.getBlock());
    	};
    PemBlock.encodeUnicode = function (_in, encodeCr) {
        if (_in == null)
            return "";
        if (!PemBlock.isUnicode(_in, '\\', encodeCr))
            return _in;
        var out = "";
        for (var i = 0; i < _in.length; i++) {
            var c = _in.charAt(i);
            if (((function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) < 32 || (function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) > 127) && (encodeCr || ((function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) != '\n'.charCodeAt(0) && (function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) != '\r'.charCodeAt(0)))) {
                var a = PemBlock.toHex2LowerString(((c).charCodeAt(0) / 256 | 0));
                var b = PemBlock.toHex2LowerString((c).charCodeAt(0) % 256);
                out = out + "\\u" + a + b;
            }
            else if ((function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) == '\\'.charCodeAt(0)) {
                out = out + "\\\\";
            }
            else
                out = out + c;
        }
        ;
        return out;
    };
    PemBlock.toHex2LowerString = function (_in) {
    		var out = _in.toString(16);
        if (out.length === 1)
            out = "0" + out;
        return out;
    };
    PemBlock.isUnicode = function (_in, _joker, encodeCr) {
        for (var i = 0; i < _in.length; i++) {
            var c = _in.charAt(i);
            if (((function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) < 32 || (function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) > 127 || (function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) == (function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(_joker)) && (encodeCr || ((function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) != '\n'.charCodeAt(0) && (function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) != '\r'.charCodeAt(0))))
                return true;
        }
        ;
        return false;
    };
    PemBlock.prototype.parse = function (block) {
        var p = block.indexOf("-----BEGIN ");
        if (p < 0)
            throw Object.defineProperty(new Error("start of block not found"), '__classes', { configurable: true, value: ['java.lang.Throwable', 'java.lang.Object', 'java.lang.Exception'] });
        block = block.substring(p + 11);
        p = block.indexOf("-----");
        if (p < 0)
            throw Object.defineProperty(new Error("end of header not found"), '__classes', { configurable: true, value: ['java.lang.Throwable', 'java.lang.Object', 'java.lang.Exception'] });
        var n = block.substring(0, p);
        if (n.indexOf("\n") != -1 || n.indexOf("\r") != -1)
            throw Object.defineProperty(new Error("name contains line break " + n), '__classes', { configurable: true, value: ['java.lang.Throwable', 'java.lang.Object', 'java.lang.Exception'] });
        this.setName(n);
        block = block.substring(p + 5);
        var endMark = "-----END " + this.getName() + "-----";
        p = block.indexOf(endMark);
        if (p < 0)
            throw Object.defineProperty(new Error("end of block not found " + this.getName()), '__classes', { configurable: true, value: ['java.lang.Throwable', 'java.lang.Object', 'java.lang.Exception'] });
        this.rest = block.substring(p + endMark.length).trim();
        block = block.substring(0, p).trim();
        var params = true;
        var blockOrg = "";
        var lastKey = null;
        while ((true)) {
            var line = block;
            p = block.indexOf('\n');
            if (p >= 0) {
                line = block.substring(0, p);
                block = block.substring(p + 1);
            }
            if (params) {
                var l = line.trim();
                if (l.length === 0) {
                    params = false;
                }
                else if ((function (str, searchString, position) {
                    if (position === void 0) { position = 0; }
                    return str.substr(position, searchString.length) === searchString;
                })(line, " ") && lastKey != null) {
                    /* put */ (this.list[lastKey] = (function (m, k) { return m[k] ? m[k] : null; })(this.list, lastKey) + line.substring(1));
                }
                else {
                    var pp = line.indexOf(':');
                    if (pp < 0) {
                        params = false;
                        blockOrg = line;
                    }
                    else {
                        lastKey = line.substring(0, pp).trim();
                        var value = line.substring(pp + 1).trim();
                        /* put */ (this.list[lastKey] = value);
                    }
                }
            }
            else {
                blockOrg = blockOrg + line;
            }
            if (p < 0)
                break;
        }
        ;
        this.block = PemBlock.decodeUnicode(blockOrg);
        return this;
    };
    PemBlock.decodeUnicode = function (_in) {
        var mode = 0;
        var buffer = [0, 0, 0, 0];
        if (_in == null)
            return "";
        var out = "";
        for (var i = 0; i < _in.length; i++) {
            var c = _in.charAt(i);
            switch ((mode)) {
                case 0:
                    if ((function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) == '\\'.charCodeAt(0))
                        mode = 1;
                    else
                        out = out + c;
                    break;
                case 1:
                    if ((function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) == 'u'.charCodeAt(0))
                        mode = 2;
                    else if ((function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) == 'n'.charCodeAt(0)) {
                        out = out + '\n';
                        mode = 0;
                    }
                    else if ((function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) == 'r'.charCodeAt(0)) {
                        out = out + '\r';
                        mode = 0;
                    }
                    else if ((function (c) { return c.charCodeAt == null ? c : c.charCodeAt(0); })(c) == 't'.charCodeAt(0)) {
                        out = out + '\t';
                        mode = 0;
                    }
                    else
                        out = out + '\\' + c;
                    break;
                case 2:
                    buffer[0] = (c).charCodeAt(0);
                    mode = 3;
                    break;
                case 3:
                    buffer[1] = (c).charCodeAt(0);
                    mode = 4;
                    break;
                case 4:
                    buffer[2] = (c).charCodeAt(0);
                    mode = 5;
                    break;
                case 5:
                    buffer[3] = (c).charCodeAt(0);
                    out = out + parseInt(buffer[0]+buffer[1]+buffer[2]+buffer[3], 16);
                    mode = 0;
                    break;
            }
        }
        ;
        return out.toString();
    };
    return PemBlock;
}());
PemBlock.BLOCK_WIDTH = 50;
PemBlock["__class"] = "PemBlock";
var PemBlockList = (function () {
    function PemBlockList(string) {
        var _this = this;
        /*private*/ this.list = ([]);
        if (((typeof string === 'string') || string === null)) {
            var __args = Array.prototype.slice.call(arguments);
            this.list = ([]);
            (function () {
                while ((true)) {
                    try {
                        var p = string.indexOf("-----BEGIN ");
                        if (p < 0)
                            break;
                        var next = new PemBlock().parse(string);
                        /* add */ (_this.list.push(next) > 0);
                        string = next.getRest();
                    }
                    catch (e) {
                        break;
                    }
                    ;
                }
                ;
            })();
        }
        else if (string === undefined) {
            var __args = Array.prototype.slice.call(arguments);
            this.list = ([]);
        }
        else
            throw new Error('invalid overload');
    }
    PemBlockList.prototype.toString$ = function () {
        var b = { str: "", toString: function () { return this.str; } };
        var _loop_2 = function (index852) {
            var block = this_1.list[index852];
            {
                /* append */ (function (sb) { sb.str = sb.str.concat(block); return sb; })(b);
            }
        };
        var this_1 = this;
        for (var index852 = 0; index852 < this.list.length; index852++) {
            _loop_2(index852);
        }
        return b.str;
    };
    PemBlockList.prototype.toString$int$int = function (offset, len) {
        var b = { str: "", toString: function () { return this.str; } };
        var cnt = 0;
        var _loop_3 = function (index853) {
            var block = this_2.list[index853];
            {
                if (cnt >= offset + len)
                    return "break";
                if (cnt >= offset)
                    (function (sb) { sb.str = sb.str.concat(block); return sb; })(b);
                cnt++;
            }
        };
        var this_2 = this;
        for (var index853 = 0; index853 < this.list.length; index853++) {
            var state_2 = _loop_3(index853);
            if (state_2 === "break")
                break;
        }
        return b.str;
    };
    PemBlockList.prototype.toString = function (offset, len) {
        if (((typeof offset === 'number') || offset === null) && ((typeof len === 'number') || len === null)) {
            return this.toString$int$int(offset, len);
        }
        else if (offset === undefined && len === undefined) {
            return this.toString$();
        }
        else
            throw new Error('invalid overload');
    };
    PemBlockList.prototype.find = function (name) {
        for (var index854 = 0; index854 < this.list.length; index854++) {
            var block = this.list[index854];
            {
                if ((function (o1, o2) { if (o1 && o1.equals) {
                    return o1.equals(o2);
                }
                else {
                    return o1 === o2;
                } })(name, block.getName()))
                    return block;
            }
        }
        return null;
    };
    return PemBlockList;
}());
PemBlockList["__class"] = "PemBlockList";
var LinearStringSplitter = (function () {
    function LinearStringSplitter() {
    }
    LinearStringSplitter.join = function () {
        var parts = [];
        for (var _i = 0; _i < arguments.length; _i++) {
            parts[_i] = arguments[_i];
        }
        var out = "";
        var cnt = 0;
        loop: while ((true)) {
            for (var index855 = 0; index855 < parts.length; index855++) {
                var part = parts[index855];
                {
                    if (cnt >= part.length)
                        break loop;
                    out = out + part.charAt(cnt);
                }
            }
            cnt++;
        }
        ;
        return out.toString();
    };
    return LinearStringSplitter;
}());
LinearStringSplitter["__class"] = "LinearStringSplitter";
