.text
Main:
	jal Fib
	halt

//********************************************************************************
// This is where the actual Fibo Calculator program starts
// Notice that in the program we just store $a0 and $ra in the stack
// $v0 is used as an accumulator since every fibo number would eventually 
// be reduced to nothing more than a bunch of base cases values added up together
//*********************************************************************************
Fib:
//push registers into stack
	addi $sp, -2
	sw $a0, 1($sp)
	sw $ra,	0($sp)

//begin the calculation	
	move $bcl,0($a0) // $bcl <= $a0 = n
	move $bcr,0($zero) // $bcr = 0 
	blt 2(case_0) // if n < 0, goto case0, else $bcr + 2
	blt 0(case_2)  // if n < 2, goto case1, else do nothing
	be 0(case_2) // if n == 2, also goto case1
	addi $bcr, 27 // $bcr = 29
	be 1(case_29)  // if n = 29, goto case29, else $bcr + 1
	be 0(case_30) // if n = 30, goto case30, else do nothing
	addi $bcr,18 //$bcr = 48
	be 1(case_48) //if n = 48, goto case48, else $bcr + 1
	be 0(case_49) //if n = 49, goto case49, else do nothing
	
case_Gen:
	addi $a0, -1 //n <= n - 1, preparing to enter the next recusive process
	jal Fib
	addi $a0, -1 // after returning from the previous recrsive call, prepare n <= n - 2
	jal Fib
	jmp restore  // return to the recursive process above this one in the hier

case_0:
	// base case
	//0x3DEADBEEF = 0011_1101_1110_1010_1101_1011_1110_1110_1111
	// = 001111_011110_101011_011011_111011_101111
	sub $t0, $t0
	sloi $t0, 0xf //001111
	sloi $t0, 0x1e //011110
	sloi $t0, 0x2b //101011
	sloi $t0, 0x1b //011011
	sloi $t0, 0x3b //111011
	sloi $t0, 0x2f //101111
	add $v0, $t0 // store the return value
	jmp restore
	
case_2: 
	//base case, when n <= 2, simply add 1 to $v0, 
	addi $v0, 1
	jmp restore

case_29:
	//514229 = 0x7D8B5 
	// = 0111 1101 1000 1011 0101
	// = 000001_111101_100010_110101
	sub $t0, $t0
	sloi $t0, 0x1 //000001
	sloi $t0, 0x3d //111101
	sloi $t0, 0x22 //100010
	sloi $t0, 0x35 //110101
	add $v0, $t0
	jmp restore
	
case_30:
	//832030 = 0xCB21E
	// = 00011_001011_001000_011110
	sub $t0, $t0
	sloi $t0, 0x3 //000011
	sloi $t0, 0xb //001011
	sloi $t0, 0x8 //001000
	sloi $t0, 0x1e //011110	
	add $v0,$t0
	jmp restore
	
case_48:
	//4807526976 = 0x11E8D0A40
	//= 000100_011110_100011_010000_101001_000000
	sloi $t0, 0x4 //000100
	sloi $t0, 0x1e //011110
	sloi $t0, 0x23 //100011
	sloi $t0, 0x10 //010000
	sloi $t0, 0x29 //101001
	sloi $t0, 0x0 //000000
	add $v0,$t0
	jmp restore
	
case_49:
	//7778742049 = 
	//000111_001111_101001_100010_111100_100001
	sloi $t0, 0x7 //000111
	sloi $t0, 0xf //001111
	sloi $t0, 0x29 //101001
	sloi $t0, 0x22 //100010
	sloi $t0, 0x3c //111100
	sloi $t0, 0x21 //100001
	add $v0,$t0
	jmp restore

// to pop values from the stack, preparing to return to the process 
//one level higher in the recursive call hierarchy
restore: 
	lw $ra, 0($sp)
	lw $a0, 1($sp)
	addi $sp, 2
	jr $ra

.data
