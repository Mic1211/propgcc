// errchk $G $D/$F.go

// Copyright 2009 The Go Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package main
func main() {
	var x int64 = 0;
	println(x != nil);	// ERROR "illegal|incompatible|nil"
	println(0 != nil);	// ERROR "illegal|incompatible|nil"
}
