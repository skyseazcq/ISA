.text
SuperGarbage:   // pc in $a0, mem in $a1
	add $a0, $a1 // $a0 = $mem[pc]
WHILE:		
	lw $bcl,0($a0) // $bcl = op
	lw $s1,1($a0) // $s1 = srcA	
	add $s1, $a1   // $s1 = &mem[srcA]	
	lw $s1,0($s1)  // $s1 = mem[srcA]
	// srcB and dest are loaded in the cases below to reduce dynamic instruction count
	//pc + 4 is executed in cases. As mem has the permanent value when first put in, 
	//we implememnt pc + 4 by &mem[pc] + 4, which would reduce dynamic instruction count, 
	//without having an effect on the executation of SG.
SWITCH:	
	sub $bcr,$bcr  // $bcr = 0, for be compare
	be 1(CASE0)    // if $bcl == $bcr, branch CASE0, else $bcr + 1 (imm) and PC + 1
	be 1(CASE1)
	be 1(CASE2)
	be 1(CASE3)
	be 1(CASE4)
	be 1(CASE5)
	be 1(CASE6)
	be 1(CASE7)
	jmp WHILE
	
CASE0:	
	lw $s2,2($a0) // $s2 = srcB
	lw $s3,3($a0) // $s3 = dest
	add $s2, $a1
	add $s3, $a1  // s2, s3 = &mem[srcB,dest]
	lw $s2,0($s2) // $s2 = mem[srcB]
	addi $a0,4  // &mem[pc] = &mem[pc] + 4
	
	sub $s1, $s2 // $s1 = mem[srcA] - mem[srcB]
	sw $s1,0($s3) // mem[dest] = mem[srcA] - mem[srcB]
	jmp WHILE
	
CASE1: 
	lw $s3,3($a0) //$s3 = dest
	add $s3, $a1  // $s3 = &mem[dest]
	addi $a0,4  // &mem[pc] = &mem[pc] + 4
	
	srl $s1,1  // $s1 = $s1 >> 1  
	sw $s1,0($s3)   // mem[dest] = mem[srcA] >> 1
	jmp WHILE

CASE2:
	lw $s2,2($a0) //$s2 = srcB
	lw $s3,3($a0) //$s3 = dest 
	add $s2, $a1
	add $s3, $a1  // $s2, $s3 = &mem[srcB,dest]
	lw $s2,0($s2) // $s2 = mem[srcB]
	addi $a0,4  // &mem[pc] = &mem[pc] + 4
	
	nor $s1, $s2
	sw $s1,0($s3) //mem[dest] = ~(mem[srcA] | mem[srcB])
	jmp WHILE
	
CASE3: 
	lw $s2,2($a0) //$s2 = srcB
	lw $s3,3($a0) //$s3 = dest 
	add $s2, $a1
	add $s3, $a1  // $s2, $s3 = &mem[srcB,dest]
	lw $s2,0($s2) // $s2 = mem[srcB] = tmp
	addi $a0,4  // &mem[pc] = &mem[pc] + 4
	
	add $s1,$a1   // $s1 = &mem[mem[srcA]
	lw $s0,0($s1) 	// $s0  = mem[mem[srcA]]
	sw $s0,0($s3) // mem[dest] = mem[mem[srcA]]
	sw $s2,0($s1) //mem[mem[srcA]] = tmp = mem[srcB]
	jmp WHILE
	
CASE4: 
	lw $s3,3($a0) //$s3 = dest
	add $s3, $a1  // $s3 = &mem[dest]
	addi $a0,4  // &mem[pc] = &mem[pc] + 4
	
	in $t0,$s1 // input from channel $s1 to $t0, where $t0 has the content from channel $s1
	sw $t0,0($s3) //store the channel content to mem[dest]
	jmp WHILE

CASE5: 
	lw $s2,2($a0) //srcB
	add $s2, $a1 // $s2 = &mem[srcB]
	lw $s2,0($s2) // $s2 = mem[srcB]
	addi $a0,4  // &mem[pc] = &mem[pc] + 4
	
	out $s1,$s2  // output $s1 = mem[srcA] to channel $s2
	jmp WHILE

CASE6: 
	lw $s2,2($a0) // $s2 = srcB
	lw $s3,3($a0) // $s3 = dest
	add $s2, $a1
	add $s3, $a1  // $s2, s3 = &mem[srcB,dest]
	lw $s2,0($s2) // $s2 = mem[srcB]
	addi $a0,4  // &mem[pc] = &mem[pc] + 4
	
	sub $a0,$a1  // $a0 = pc
	sw $a0,0($s3) //store pc to mem[dest]
	sub $bcr,$bcr   //$bcr = 0
	move $bcl, 0($s1)  // $bcl = mem[srcA]
	blt 0(NEXT)       // if $bcl < $bcr (mem[srcA] < 0), branch NEXT
	add $a0,$a1    //$a0 = &mem[pc]
	jmp WHILE
NEXT:
	move $a0, 0($s2) // $a0 = pc = mem[srcB] + 0
	add $a0,$a1    //$a0 = &mem[pc]
	jmp WHILE
	
CASE7:
	addi $a0,4  // $a0 = &mem[pc] = &mem[pc] + 4
	sub $a0,$a1    // $a0 = $a0 - $a1 = &mem[pc] - mem = pc
	move $v0, 0($a0)  // return $v0 = pc
	add $a0,$a1   // $a0 = &mem[pc]
	halt
